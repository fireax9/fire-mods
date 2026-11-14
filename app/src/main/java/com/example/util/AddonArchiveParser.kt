package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.util.Locale
import java.util.zip.ZipFile
import org.json.JSONObject

data class ArchiveEntrySummary(
    val path: String,
    val sizeBytes: Long,
)

data class ParsedArchiveInfo(
    val title: String,
    val edition: String,          // JAVA, BEDROCK
    val category: String,         // MOD, MODPACK, RESOURCE_PACK, SHADER
    val loaderOrFormat: String,
    val versionName: String,
    val description: String,
    val entries: List<ArchiveEntrySummary>,
)

object AddonArchiveParser {

    fun inspectArchiveFile(
        file: File,
        fallbackTitle: String,
        preferredCategory: String?,
        preferredEdition: String?,
    ): ParsedArchiveInfo {
        val ext = file.extension.lowercase()
        val entries = mutableListOf<ArchiveEntrySummary>()
        var detectedTitle = fallbackTitle.ifBlank { file.nameWithoutExtension }
        var detectedEdition = preferredEdition ?: if (ext in listOf("mcpack", "mcaddon", "mcworld", "mctemplate")) "BEDROCK" else "JAVA"
        var detectedCategory = preferredCategory ?: "MOD"
        var detectedLoader = if (ext.isNotBlank()) ".$ext" else "Archive"
        var detectedVersion = "1.0"
        var detectedDesc = ""

        runCatching {
            ZipFile(file).use { zip ->
                val enumEntries = zip.entries()
                var hasModrinthIndex = false
                var hasBedrockManifest = false
                var hasFabricJson = false
                var hasModsToml = false
                var hasPackMcmeta = false
                var hasShadersDir = false

                while (enumEntries.hasMoreElements()) {
                    val entry = enumEntries.nextElement()
                    if (!entry.isDirectory) {
                        if (entries.size < 40) {
                            entries += ArchiveEntrySummary(entry.name, entry.size.coerceAtLeast(0L))
                        }
                        val lower = entry.name.lowercase()
                        when {
                            lower == "modrinth.index.json" -> {
                                hasModrinthIndex = true
                                val text = zip.getInputStream(entry).bufferedReader().readText()
                                val json = JSONObject(text)
                                detectedTitle = json.optString("name", detectedTitle)
                                detectedVersion = json.optString("versionId", detectedVersion)
                                detectedEdition = "JAVA"
                                detectedCategory = "MODPACK"
                                val deps = json.optJSONObject("dependencies")
                                detectedLoader = when {
                                    deps?.has("fabric-loader") == true -> "Fabric"
                                    deps?.has("neoforge") == true -> "NeoForge"
                                    deps?.has("forge") == true -> "Forge"
                                    deps?.has("quilt-loader") == true -> "Quilt"
                                    else -> "Modrinth Pack"
                                }
                                val filesArr = json.optJSONArray("files")
                                if (filesArr != null && entries.size <= 1) {
                                    entries.clear()
                                    for (i in 0 until minOf(filesArr.length(), 40)) {
                                        val obj = filesArr.optJSONObject(i) ?: continue
                                        entries += ArchiveEntrySummary(
                                            path = obj.optString("path", "file_$i"),
                                            sizeBytes = obj.optLong("fileSize", 0L),
                                        )
                                    }
                                }
                            }
                            lower == "manifest.json" || lower.endsWith("/manifest.json") -> {
                                hasBedrockManifest = true
                                val text = zip.getInputStream(entry).bufferedReader().readText()
                                val json = JSONObject(text)
                                val header = json.optJSONObject("header")
                                if (header != null) {
                                    val name = header.optString("name", "").trim()
                                    if (name.isNotBlank() && !name.startsWith("pack.")) {
                                        detectedTitle = name
                                    }
                                    detectedDesc = header.optString("description", "")
                                }
                                val modules = json.optJSONArray("modules")
                                val types = mutableListOf<String>()
                                if (modules != null) {
                                    for (i in 0 until modules.length()) {
                                        types += modules.optJSONObject(i)?.optString("type", "").orEmpty()
                                    }
                                }
                                detectedEdition = "BEDROCK"
                                detectedLoader = if (ext.isNotBlank()) ".$ext" else ".mcpack"
                                if (preferredCategory == null) {
                                    detectedCategory = when {
                                        types.any { it.equals("data", true) || it.equals("script", true) } -> "MOD"
                                        types.any { it.equals("resources", true) } -> "RESOURCE_PACK"
                                        else -> "MOD"
                                    }
                                }
                            }
                            lower == "fabric.mod.json" -> {
                                hasFabricJson = true
                                val json = JSONObject(zip.getInputStream(entry).bufferedReader().readText())
                                detectedTitle = json.optString("name", detectedTitle)
                                detectedVersion = json.optString("version", detectedVersion)
                                detectedDesc = json.optString("description", "")
                                detectedLoader = "Fabric"
                                detectedEdition = "JAVA"
                                detectedCategory = "MOD"
                            }
                            lower == "meta-inf/mods.toml" || lower == "meta-inf/neoforge.mods.toml" -> {
                                hasModsToml = true
                                detectedLoader = if (lower.contains("neoforge")) "NeoForge" else "Forge"
                                detectedEdition = "JAVA"
                                detectedCategory = "MOD"
                            }
                            lower == "pack.mcmeta" -> {
                                hasPackMcmeta = true
                                val json = JSONObject(zip.getInputStream(entry).bufferedReader().readText())
                                val packObj = json.optJSONObject("pack")
                                val desc = packObj?.optString("description", "").orEmpty()
                                if (desc.isNotBlank()) detectedDesc = desc
                            }
                            lower.startsWith("shaders/") -> {
                                hasShadersDir = true
                            }
                        }
                    }
                }

                if (!hasModrinthIndex && !hasBedrockManifest && !hasFabricJson && !hasModsToml) {
                    if (hasShadersDir) {
                        detectedCategory = preferredCategory ?: "SHADER"
                        detectedLoader = "Iris / OptiFine"
                    } else if (hasPackMcmeta) {
                        detectedCategory = preferredCategory ?: "RESOURCE_PACK"
                        detectedLoader = "Resource Pack"
                    }
                }
            }
        }

        return ParsedArchiveInfo(
            title = detectedTitle,
            edition = detectedEdition,
            category = detectedCategory,
            loaderOrFormat = detectedLoader,
            versionName = detectedVersion,
            description = detectedDesc,
            entries = entries,
        )
    }

    fun openAddonWithMinecraftOrChooser(context: Context, filePath: String) {
        val file = File(filePath)
        if (!file.exists()) return
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val mime = when (file.extension.lowercase()) {
            "mcpack", "mcaddon", "mcworld", "mctemplate" -> "application/octet-stream"
            "zip", "mrpack" -> "application/zip"
            "jar" -> "application/java-archive"
            else -> "*/*"
        }
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (viewIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(Intent.createChooser(viewIntent, "Import ${file.name}"))
        } else {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mime
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share ${file.name}"))
        }
    }

    fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024L * 1024L -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
            bytes >= 1024L -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }

    fun formatDownloads(count: Long): String {
        return when {
            count >= 1_000_000L -> String.format(Locale.US, "%.1fM", count / 1_000_000.0)
            count >= 1_000L -> String.format(Locale.US, "%.1fK", count / 1_000.0)
            else -> count.toString()
        }
    }
}
