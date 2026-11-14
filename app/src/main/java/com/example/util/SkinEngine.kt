package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.content.FileProvider
import com.example.data.local.SavedSkinEntity
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object SkinEngine {

    private data class StarterPreset(
        val id: String,
        val name: String,
        val modelType: String,
        val skinColor: Int,
        val primaryColor: Int,
        val secondaryColor: Int,
        val accentColor: Int,
        val eyeColor: Int,
    )

    private val starterPresets = listOf(
        StarterPreset(
            id = "skin_michich_nether",
            name = "Michich Nether Knight",
            modelType = "CLASSIC",
            skinColor = Color.rgb(235, 195, 165),
            primaryColor = Color.rgb(32, 24, 38),
            secondaryColor = Color.rgb(230, 57, 70),
            accentColor = Color.rgb(255, 183, 3),
            eyeColor = Color.rgb(255, 183, 3),
        ),
        StarterPreset(
            id = "skin_volcanic_blaze",
            name = "Volcanic Blaze",
            modelType = "SLIM",
            skinColor = Color.rgb(218, 170, 140),
            primaryColor = Color.rgb(166, 28, 38),
            secondaryColor = Color.rgb(255, 107, 53),
            accentColor = Color.rgb(255, 214, 10),
            eyeColor = Color.rgb(255, 230, 109),
        ),
        StarterPreset(
            id = "skin_obsidian_sentinel",
            name = "Obsidian Sentinel",
            modelType = "CLASSIC",
            skinColor = Color.rgb(180, 185, 200),
            primaryColor = Color.rgb(20, 22, 34),
            secondaryColor = Color.rgb(75, 45, 120),
            accentColor = Color.rgb(168, 85, 247),
            eyeColor = Color.rgb(56, 189, 248),
        ),
        StarterPreset(
            id = "skin_emerald_warden",
            name = "Emerald Warden",
            modelType = "SLIM",
            skinColor = Color.rgb(230, 190, 160),
            primaryColor = Color.rgb(18, 58, 46),
            secondaryColor = Color.rgb(16, 185, 129),
            accentColor = Color.rgb(255, 183, 3),
            eyeColor = Color.rgb(52, 211, 153),
        ),
    )

    fun normalizeSkinBitmap(source: Bitmap): Bitmap? {
        if (source.width == 64 && source.height == 64) {
            return source.copy(Bitmap.Config.ARGB_8888, true)
        }
        if (source.width == 64 && source.height == 32) {
            val upgraded = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(upgraded)
            val paint = Paint().apply { isFilterBitmap = false }
            canvas.drawBitmap(source, 0f, 0f, paint)
            // Mirror right leg (0..16, 16..32) to left leg (16..32, 48..64)
            canvas.drawBitmap(source, Rect(0, 16, 16, 32), Rect(16, 48, 32, 64), paint)
            // Mirror right arm (40..56, 16..32) to left arm (32..48, 48..64)
            canvas.drawBitmap(source, Rect(40, 16, 56, 32), Rect(32, 48, 48, 64), paint)
            return upgraded
        }
        if (source.width >= 64 && source.height >= 64 && source.width == source.height) {
            return Bitmap.createScaledBitmap(source, 64, 64, false)
        }
        return null
    }

    fun detectSlimOrClassic(bitmap: Bitmap): String {
        if (bitmap.width < 64 || bitmap.height < 64) return "CLASSIC"
        val alpha = Color.alpha(bitmap.getPixel(54, 20))
        return if (alpha == 0) "SLIM" else "CLASSIC"
    }

    fun seedStarterSkinsIfNeeded(context: Context): List<SavedSkinEntity> {
        val dir = File(context.filesDir, "skins").apply { if (!exists()) mkdirs() }
        return starterPresets.mapIndexed { index, preset ->
            val file = File(dir, "${preset.id}.png")
            if (!file.exists()) {
                val bmp = generateProceduralSkin(preset)
                FileOutputStream(file).use { out ->
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            }
            SavedSkinEntity(
                id = preset.id,
                name = preset.name,
                modelType = preset.modelType,
                source = "BUILTIN",
                filePath = file.absolutePath,
                isSelected = index == 0,
                updatedAt = System.currentTimeMillis() - index * 1000L,
            )
        }
    }

    fun saveBitmapToSkinFile(context: Context, bitmap: Bitmap, id: String): File {
        val dir = File(context.filesDir, "skins").apply { if (!exists()) mkdirs() }
        val outFile = File(dir, "$id.png")
        FileOutputStream(outFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return outFile
    }

    fun loadSkinBitmap(filePath: String): Bitmap? {
        val file = File(filePath)
        if (!file.exists()) return null
        return BitmapFactory.decodeFile(file.absolutePath)
    }

    fun exportSkinAsBedrockMcpack(context: Context, skin: SavedSkinEntity): Result<File> = runCatching {
        val srcFile = File(skin.filePath)
        require(srcFile.exists()) { "Skin file missing" }
        val exportsDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
        val safeName = skin.name.replace(Regex("[^A-Za-z0-9_-]"), "_").ifBlank { "michich_skin" }
        val outFile = File(exportsDir, "${safeName}_skinpack.mcpack")

        val headerUuid = UUID.randomUUID().toString()
        val moduleUuid = UUID.randomUUID().toString()
        val geometry = if (skin.modelType == "SLIM") "geometry.humanoid.customSlim" else "geometry.humanoid.custom"

        val manifestJson = """
            {
              "format_version": 2,
              "header": {
                "name": "${skin.name} Skin Pack",
                "version": [1, 0, 0],
                "uuid": "$headerUuid"
              },
              "modules": [
                {
                  "type": "skin_pack",
                  "version": [1, 0, 0],
                  "uuid": "$moduleUuid"
                }
              ]
            }
        """.trimIndent()

        val skinsJson = """
            {
              "skins": [
                {
                  "localization_name": "$safeName",
                  "geometry": "$geometry",
                  "texture": "skin.png",
                  "type": "free"
                }
              ],
              "serialize_name": "MichichSkins",
              "localization_name": "MichichSkins"
            }
        """.trimIndent()

        val langContent = "skinpack.MichichSkins=Michich Skin Studio\nskin.MichichSkins.$safeName=${skin.name}\n"

        ZipOutputStream(FileOutputStream(outFile)).use { zos ->
            zos.putNextEntry(ZipEntry("manifest.json"))
            zos.write(manifestJson.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("skins.json"))
            zos.write(skinsJson.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("texts/en_US.lang"))
            zos.write(langContent.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("skin.png"))
            zos.write(srcFile.readBytes())
            zos.closeEntry()
        }

        outFile
    }

    fun openOrShareExportedFile(context: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (viewIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(Intent.createChooser(viewIntent, "Import ${file.name}"))
        } else {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share ${file.name}"))
        }
    }

    private fun generateProceduralSkin(p: StarterPreset): Bitmap {
        val bmp = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        fun fillRect(x0: Int, y0: Int, w: Int, h: Int, c: Int) {
            for (y in y0 until (y0 + h).coerceAtMost(64)) {
                for (x in x0 until (x0 + w).coerceAtMost(64)) {
                    bmp.setPixel(x, y, c)
                }
            }
        }

        // Head base (0..32, 0..16)
        fillRect(0, 0, 32, 16, p.skinColor)
        // Hair / helmet top & back
        fillRect(8, 0, 8, 8, p.primaryColor)
        fillRect(16, 0, 8, 8, p.primaryColor)
        fillRect(24, 8, 8, 8, p.primaryColor)
        fillRect(0, 8, 8, 4, p.primaryColor)
        fillRect(16, 8, 8, 4, p.primaryColor)
        // Face hair fringe & eyes (front face is 8..16, 8..16)
        fillRect(8, 8, 8, 2, p.primaryColor)
        bmp.setPixel(9, 11, Color.WHITE)
        bmp.setPixel(10, 11, p.eyeColor)
        bmp.setPixel(13, 11, p.eyeColor)
        bmp.setPixel(14, 11, Color.WHITE)
        // Crown / gold visor accent on Hat layer (40..48, 8..16)
        fillRect(40, 8, 8, 2, p.accentColor)
        fillRect(40, 10, 1, 2, p.secondaryColor)
        fillRect(47, 10, 1, 2, p.secondaryColor)

        // Torso (16..40, 16..32)
        fillRect(16, 16, 24, 16, p.primaryColor)
        // Chest emblem on front (20..28, 20..32)
        fillRect(22, 22, 4, 4, p.secondaryColor)
        fillRect(23, 23, 2, 2, p.accentColor)
        // Belt
        fillRect(20, 29, 8, 2, p.accentColor)

        // Right Arm (40..56, 16..32) & Left Arm (32..48, 48..64)
        val armWidth = if (p.modelType == "SLIM") 3 else 4
        fillRect(40, 16, armWidth * 4, 16, p.primaryColor)
        fillRect(44, 24, armWidth, 4, p.secondaryColor)
        fillRect(32, 48, armWidth * 4, 16, p.primaryColor)
        fillRect(36, 56, armWidth, 4, p.secondaryColor)

        // Right Leg (0..16, 16..32) & Left Leg (16..32, 48..64)
        fillRect(0, 16, 16, 16, p.primaryColor)
        fillRect(4, 28, 4, 4, p.accentColor)
        fillRect(16, 48, 16, 16, p.primaryColor)
        fillRect(20, 60, 4, 4, p.accentColor)

        return bmp
    }
}
