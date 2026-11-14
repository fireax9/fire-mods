package com.example.data.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import com.example.data.local.DownloadedAddonEntity
import com.example.data.local.LauncherDao
import com.example.data.local.SavedSkinEntity
import com.example.data.remote.ModrinthHit
import com.example.data.remote.ModrinthVersion
import com.example.data.remote.NetworkModule
import com.example.util.AddonArchiveParser
import com.example.util.SkinEngine
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject

class LauncherRepository(
    private val context: Context,
    private val dao: LauncherDao,
) {
    val addons: Flow<List<DownloadedAddonEntity>> = dao.observeAddons()
    val skins: Flow<List<SavedSkinEntity>> = dao.observeSkins()

    suspend fun ensureStarterSkins() = withContext(Dispatchers.IO) {
        if (dao.getSkinsOnce().isEmpty()) {
            SkinEngine.seedStarterSkinsIfNeeded(context).forEach { dao.upsertSkin(it) }
        }
    }

    suspend fun searchModrinth(
        query: String,
        category: String,
        sortIndex: String = "downloads",
    ): Result<List<ModrinthHit>> = withContext(Dispatchers.IO) {
        runCatching {
            val projectType = when (category) {
                "MOD" -> "mod"
                "MODPACK" -> "modpack"
                "RESOURCE_PACK" -> "resourcepack"
                "SHADER" -> "shader"
                else -> "mod"
            }
            val facets = "[[\"project_type:$projectType\"]]"
            NetworkModule.modrinthApi.searchProjects(
                query = query.trim(),
                facets = facets,
                index = sortIndex,
                limit = 25,
                offset = 0,
            ).hits
        }
    }

    suspend fun getModrinthVersions(projectId: String): Result<List<ModrinthVersion>> =
        withContext(Dispatchers.IO) {
            runCatching {
                NetworkModule.modrinthApi.getProjectVersions(projectId)
            }
        }

    suspend fun downloadAddon(
        projectId: String,
        title: String,
        category: String,
        edition: String,
        source: String,
        versionName: String,
        fileName: String,
        downloadUrl: String,
        iconUrl: String,
        loaderOrFormat: String,
    ): Result<DownloadedAddonEntity> = withContext(Dispatchers.IO) {
        val vaultDir = File(context.filesDir, "vault").apply { if (!exists()) mkdirs() }
        val cleanName = fileName.replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "addon.zip" }
        val destFile = File(vaultDir, "${System.currentTimeMillis()}_$cleanName")
        val addonId = UUID.randomUUID().toString()

        val initial = DownloadedAddonEntity(
            id = addonId,
            projectId = projectId,
            title = title.ifBlank { cleanName },
            category = category,
            edition = edition,
            source = source,
            versionName = versionName,
            fileName = cleanName,
            filePath = destFile.absolutePath,
            iconUrl = iconUrl,
            loaderOrFormat = loaderOrFormat,
            sizeBytes = 0L,
            status = "DOWNLOADING",
            progress = 5,
        )
        dao.upsertAddon(initial)

        runCatching {
            val req = Request.Builder().url(downloadUrl).build()
            NetworkModule.okHttpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) error("HTTP ${resp.code}")
                val body = resp.body ?: error("Empty download body")
                val total = body.contentLength().coerceAtLeast(1L)
                body.byteStream().use { input ->
                    FileOutputStream(destFile).use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead = 0L
                        var lastPct = 5
                        var n: Int
                        while (input.read(buffer).also { n = it } != -1) {
                            output.write(buffer, 0, n)
                            bytesRead += n
                            val pct = ((bytesRead * 100) / total).toInt().coerceIn(5, 99)
                            if (pct - lastPct >= 10) {
                                lastPct = pct
                                dao.upsertAddon(initial.copy(sizeBytes = bytesRead, progress = pct))
                            }
                        }
                    }
                }
            }

            val parsed = AddonArchiveParser.inspectArchiveFile(
                file = destFile,
                fallbackTitle = title,
                preferredCategory = category,
                preferredEdition = edition,
            )
            val ready = initial.copy(
                title = parsed.title.ifBlank { initial.title },
                edition = parsed.edition,
                category = parsed.category,
                loaderOrFormat = parsed.loaderOrFormat.ifBlank { initial.loaderOrFormat },
                sizeBytes = destFile.length(),
                status = "READY",
                progress = 100,
            )
            dao.upsertAddon(ready)
            ready
        }.onFailure {
            dao.upsertAddon(initial.copy(status = "FAILED", progress = 0))
        }
    }

    suspend fun importLocalAddon(uri: Uri): Result<DownloadedAddonEntity> = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver
            var displayName = "imported_addon.zip"
            resolver.query(uri, null, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && cursor.moveToFirst()) {
                    displayName = cursor.getString(idx) ?: displayName
                }
            }
            val cleanName = displayName.replace(Regex("[^A-Za-z0-9._-]"), "_")
            val vaultDir = File(context.filesDir, "vault").apply { if (!exists()) mkdirs() }
            val destFile = File(vaultDir, "${System.currentTimeMillis()}_$cleanName")

            resolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: error("Cannot read selected file")

            val parsed = AddonArchiveParser.inspectArchiveFile(
                file = destFile,
                fallbackTitle = cleanName.substringBeforeLast('.'),
                preferredCategory = null,
                preferredEdition = null,
            )
            val entity = DownloadedAddonEntity(
                id = UUID.randomUUID().toString(),
                projectId = "local_${UUID.randomUUID()}",
                title = parsed.title,
                category = parsed.category,
                edition = parsed.edition,
                source = "LOCAL",
                versionName = parsed.versionName,
                fileName = cleanName,
                filePath = destFile.absolutePath,
                loaderOrFormat = parsed.loaderOrFormat,
                sizeBytes = destFile.length(),
                status = "READY",
                progress = 100,
            )
            dao.upsertAddon(entity)
            entity
        }
    }

    suspend fun deleteAddon(addon: DownloadedAddonEntity) = withContext(Dispatchers.IO) {
        runCatching { File(addon.filePath).delete() }
        dao.deleteAddonById(addon.id)
    }

    suspend fun selectSkin(id: String) = withContext(Dispatchers.IO) {
        dao.clearSelectedSkins()
        dao.markSkinSelected(id)
    }

    suspend fun fetchMojangSkin(username: String): Result<SavedSkinEntity> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanUser = username.trim()
            require(cleanUser.isNotBlank()) { "Enter a Minecraft username" }
            val profile = NetworkModule.mojangApi.getUuidByUsername(cleanUser)
            val session = NetworkModule.mojangApi.getSessionProfile(profile.id)
            val texturesProp = session.properties.firstOrNull { it.name == "textures" }
                ?: error("No skin texture found for ${profile.name}")
            val decoded = String(Base64.decode(texturesProp.value, Base64.DEFAULT), Charsets.UTF_8)
            val texturesJson = JSONObject(decoded).getJSONObject("textures")
            val skinObj = texturesJson.getJSONObject("SKIN")
            val skinUrl = skinObj.getString("url")
            val isSlim = skinObj.optJSONObject("metadata")?.optString("model") == "slim"

            val req = Request.Builder().url(skinUrl).build()
            val bytes = NetworkModule.okHttpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) error("HTTP ${resp.code}")
                resp.body?.bytes() ?: error("Empty skin image")
            }
            val rawBmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: error("Invalid PNG skin")
            val normalized = SkinEngine.normalizeSkinBitmap(rawBmp) ?: error("Unsupported skin dimensions")
            val id = "mojang_${profile.id}_${System.currentTimeMillis()}"
            val savedFile = SkinEngine.saveBitmapToSkinFile(context, normalized, id)

            val entity = SavedSkinEntity(
                id = id,
                name = profile.name,
                modelType = if (isSlim) "SLIM" else SkinEngine.detectSlimOrClassic(normalized),
                source = "MOJANG",
                filePath = savedFile.absolutePath,
                isSelected = true,
                updatedAt = System.currentTimeMillis(),
            )
            dao.clearSelectedSkins()
            dao.upsertSkin(entity)
            entity
        }
    }

    suspend fun importSkinFromUri(uri: Uri): Result<SavedSkinEntity> = withContext(Dispatchers.IO) {
        runCatching {
            val rawBmp = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it)
            } ?: error("Cannot decode image")
            val normalized = SkinEngine.normalizeSkinBitmap(rawBmp)
                ?: error("Skin image must be 64x64 or 64x32 PNG")
            val id = "imported_${UUID.randomUUID()}"
            val savedFile = SkinEngine.saveBitmapToSkinFile(context, normalized, id)
            val entity = SavedSkinEntity(
                id = id,
                name = "Custom Skin",
                modelType = SkinEngine.detectSlimOrClassic(normalized),
                source = "IMPORTED",
                filePath = savedFile.absolutePath,
                isSelected = true,
                updatedAt = System.currentTimeMillis(),
            )
            dao.clearSelectedSkins()
            dao.upsertSkin(entity)
            entity
        }
    }

    suspend fun saveEditedSkin(baseSkin: SavedSkinEntity, editedBitmap: android.graphics.Bitmap): SavedSkinEntity =
        withContext(Dispatchers.IO) {
            val file = SkinEngine.saveBitmapToSkinFile(context, editedBitmap, baseSkin.id)
            val updated = baseSkin.copy(
                filePath = file.absolutePath,
                source = if (baseSkin.source == "BUILTIN") "EDITED" else baseSkin.source,
                updatedAt = System.currentTimeMillis(),
            )
            dao.upsertSkin(updated)
            updated
        }

    suspend fun deleteSkin(skin: SavedSkinEntity) = withContext(Dispatchers.IO) {
        runCatching { File(skin.filePath).delete() }
        dao.deleteSkinById(skin.id)
    }
}
