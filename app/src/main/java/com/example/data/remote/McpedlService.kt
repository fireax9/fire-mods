package com.example.data.remote

import java.net.URLEncoder

data class McpedlPortalShortcut(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val url: String,
)

object McpedlService {

    private const val BASE_URL = "https://mcpedl.com"

    fun buildCategoryUrl(category: String): String {
        return when (category) {
            "MOD" -> "$BASE_URL/category/mods/"
            "MODPACK" -> "$BASE_URL/category/maps/"
            "RESOURCE_PACK" -> "$BASE_URL/category/texture-packs/"
            "SHADER" -> "$BASE_URL/category/shaders/"
            else -> BASE_URL
        }
    }

    fun buildSearchUrl(query: String, category: String): String {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return buildCategoryUrl(category)
        val encoded = URLEncoder.encode(trimmed, "UTF-8")
        return "$BASE_URL/?s=$encoded"
    }

    fun isDirectAddonDownload(url: String): Boolean {
        val lower = url.lowercase().substringBefore("?")
        return lower.endsWith(".mcpack") ||
            lower.endsWith(".mcaddon") ||
            lower.endsWith(".mcworld") ||
            lower.endsWith(".mctemplate") ||
            lower.endsWith(".zip")
    }

    fun getPortalsForCategory(category: String): List<McpedlPortalShortcut> {
        return when (category) {
            "MOD" -> listOf(
                McpedlPortalShortcut("mcpedl_mods", "MCPEDL Addons & Mods", "Browse latest Bedrock Behavior & Resource Addons", "MOD", "$BASE_URL/category/mods/"),
                McpedlPortalShortcut("mcpedl_scripts", "Bedrock Scripts & Guns", "Explore Script API and custom item packs", "MOD", "$BASE_URL/tag/addon/"),
            )
            "MODPACK" -> listOf(
                McpedlPortalShortcut("mcpedl_maps", "MCPEDL Worlds & Modpacks", "Adventure maps, survival hubs, and bundled worlds", "MODPACK", "$BASE_URL/category/maps/"),
                McpedlPortalShortcut("mcpedl_survival", "Survival Worlds", "Custom survival spawns and progression packs", "MODPACK", "$BASE_URL/category/maps/survival-maps/"),
            )
            "RESOURCE_PACK" -> listOf(
                McpedlPortalShortcut("mcpedl_textures", "MCPEDL Texture Packs", "16x, 32x, 64x & 128x Bedrock Resource Packs", "RESOURCE_PACK", "$BASE_URL/category/texture-packs/"),
                McpedlPortalShortcut("mcpedl_pvp", "Bedrock UI & PvP Packs", "Clean HUDs, low fire, and outlined ores", "RESOURCE_PACK", "$BASE_URL/tag/pvp-texture-packs/"),
            )
            "SHADER" -> listOf(
                McpedlPortalShortcut("mcpedl_shaders", "MCPEDL Shaders & RenderDragon", "Vibrant Visuals, Deferred Technical Preview & PBR shaders", "SHADER", "$BASE_URL/category/shaders/"),
                McpedlPortalShortcut("mcpedl_pbr", "Deferred / PBR Lighting", "Dynamic shadows, volumetric fog & reflections", "SHADER", "$BASE_URL/tag/shaders/"),
            )
            else -> emptyList()
        }
    }
}
