package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.MichichDatabase
import com.example.data.remote.McpedlService
import com.example.data.repository.LauncherRepository
import com.example.ui.LauncherViewModel
import com.example.ui.theme.AppColorTheme
import com.example.ui.theme.AppShapeStyle
import com.example.util.AddonArchiveParser
import com.example.util.SkinEngine
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches Michich Mods Hub`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Michich Mods Hub", appName)
    }

    @Test
    fun `skin engine upgrades 64x32 skin and exports valid bedrock mcpack`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val legacy = Bitmap.createBitmap(64, 32, Bitmap.Config.ARGB_8888)
        legacy.eraseColor(Color.RED)
        val upgraded = SkinEngine.normalizeSkinBitmap(legacy)
        assertNotNull(upgraded)
        assertEquals(64, upgraded!!.width)
        assertEquals(64, upgraded.height)

        upgraded.setPixel(54, 20, Color.TRANSPARENT)
        assertEquals("SLIM", SkinEngine.detectSlimOrClassic(upgraded))

        val seeded = SkinEngine.seedStarterSkinsIfNeeded(context)
        assertEquals(4, seeded.size)
        val mcpackResult = SkinEngine.exportSkinAsBedrockMcpack(context, seeded.first())
        assertTrue(mcpackResult.isSuccess)
        assertTrue(mcpackResult.getOrThrow().exists())
    }

    @Test
    fun `addon archive parser reads java mrpack and bedrock mcpack manifests`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val mrpackFile = File(context.cacheDir, "test_pack.mrpack")
        val indexJson = """
            {
              "formatVersion": 1,
              "game": "minecraft",
              "versionId": "1.2.0",
              "name": "Nether Forge Pack",
              "dependencies": {
                "minecraft": "1.21.4",
                "fabric-loader": "0.16.10"
              },
              "files": [
                {
                  "path": "mods/sodium-fabric-0.6.0.jar",
                  "hashes": { "sha1": "abc123" },
                  "downloads": ["https://example.com/sodium.jar"],
                  "fileSize": 1048576
                }
              ]
            }
        """.trimIndent()
        ZipOutputStream(FileOutputStream(mrpackFile)).use { zos ->
            zos.putNextEntry(ZipEntry("modrinth.index.json"))
            zos.write(indexJson.toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }

        val parsedJava = AddonArchiveParser.inspectArchiveFile(mrpackFile, "test_pack.mrpack", null, null)
        assertEquals("JAVA", parsedJava.edition)
        assertEquals("Nether Forge Pack", parsedJava.title)
        assertEquals("Fabric", parsedJava.loaderOrFormat)
        assertEquals(1, parsedJava.entries.size)

        val mcpackFile = File(context.cacheDir, "nether_shader.mcpack")
        val bedrockManifest = """
            {
              "format_version": 2,
              "header": {
                "name": "Nether Luminous Shader",
                "description": "Bedrock Shader"
              },
              "modules": [
                { "type": "resources" }
              ]
            }
        """.trimIndent()
        ZipOutputStream(FileOutputStream(mcpackFile)).use { zos ->
            zos.putNextEntry(ZipEntry("manifest.json"))
            zos.write(bedrockManifest.toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }

        val parsedBedrock = AddonArchiveParser.inspectArchiveFile(mcpackFile, "nether_shader.mcpack", null, null)
        assertEquals("BEDROCK", parsedBedrock.edition)
        assertEquals("Nether Luminous Shader", parsedBedrock.title)
        assertEquals(".mcpack", parsedBedrock.loaderOrFormat)
    }

    @Test
    fun `mcpedl search url builder encodes query cleanly`() {
        val url = McpedlService.buildSearchUrl("better bedrock", "MOD")
        assertTrue(url.contains("mcpedl.com"))
        assertTrue(url.contains("better+bedrock"))
    }

    @Test
    fun `repository seeds starter skins in Room`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, MichichDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = LauncherRepository(context, db.launcherDao())
            repo.ensureStarterSkins()
            val skins = repo.skins.first()
            assertEquals(4, skins.size)
            assertTrue(skins.any { it.name == "Michich Nether Knight" && it.isSelected })
        } finally {
            db.close()
        }
    }

    @Test
    fun `appearance themes and shapes persist in LauncherViewModel`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = LauncherViewModel(app)

        assertEquals(4, AppColorTheme.entries.size)
        assertEquals(3, AppShapeStyle.entries.size)

        vm.setColorTheme(AppColorTheme.TEAL)
        vm.setShapeStyle(AppShapeStyle.BLOCKY)
        assertEquals(AppColorTheme.TEAL, vm.colorTheme.value)
        assertEquals(AppShapeStyle.BLOCKY, vm.shapeStyle.value)

        vm.setColorTheme(AppColorTheme.PURPLE)
        vm.setShapeStyle(AppShapeStyle.PILL)

        val reloadedVm = LauncherViewModel(app)
        assertEquals(AppColorTheme.PURPLE, reloadedVm.colorTheme.value)
        assertEquals(AppShapeStyle.PILL, reloadedVm.shapeStyle.value)
    }
}
