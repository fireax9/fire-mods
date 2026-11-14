package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Base Obsidian Surfaces
val ObsidianBg = Color(0xFF0A0B10)
val ObsidianSurface = Color(0xFF13151E)
val ObsidianCard = Color(0xFF1A1D28)
val ObsidianElevated = Color(0xFF232736)
val ObsidianBorder = Color(0xFF2E3347)

// Theme Accent Colors
val VolcanicRed = Color(0xFFE63946)
val VolcanicRedBright = Color(0xFFFF6B6B)

val NetherGold = Color(0xFFFFB703)
val MagmaOrange = Color(0xFFFF6B35)

val TealCyan = Color(0xFF14B8A6)
val SkyCyan = Color(0xFF38BDF8)

val AmethystPurple = Color(0xFFA855F7)
val VioletGlow = Color(0xFFC084FC)

// Semantic Accents
val EmeraldAccent = Color(0xFF10B981)
val ModrinthGreen = Color(0xFF1BD96A)
val BedrockCyan = Color(0xFF38BDF8)

val TextPrimary = Color(0xFFF3F4F8)
val TextSecondary = Color(0xFFA0A7BA)
val TextMuted = Color(0xFF6B7285)

enum class AppColorTheme(
    val key: String,
    val label: String,
    val primary: Color,
    val secondary: Color,
    val containerTint: Color,
) {
    RED(
        key = "RED",
        label = "Red",
        primary = VolcanicRed,
        secondary = VolcanicRedBright,
        containerTint = Color(0xFF3A1419),
    ),
    ORANGE(
        key = "ORANGE",
        label = "Orange",
        primary = NetherGold,
        secondary = MagmaOrange,
        containerTint = Color(0xFF3A280A),
    ),
    TEAL(
        key = "TEAL",
        label = "Teal",
        primary = TealCyan,
        secondary = SkyCyan,
        containerTint = Color(0xFF0D3332),
    ),
    PURPLE(
        key = "PURPLE",
        label = "Purple",
        primary = AmethystPurple,
        secondary = VioletGlow,
        containerTint = Color(0xFF2E1747),
    );

    companion object {
        fun fromKey(key: String?): AppColorTheme =
            entries.firstOrNull { it.key == key } ?: ORANGE
    }
}

enum class AppShapeStyle(
    val key: String,
    val label: String,
    val smallDp: Int,
    val mediumDp: Int,
    val largeDp: Int,
) {
    BLOCKY("BLOCKY", "Blocky", 4, 6, 8),
    ROUNDED("ROUNDED", "Rounded", 12, 16, 20),
    PILL("PILL", "Pill", 18, 24, 28);

    companion object {
        fun fromKey(key: String?): AppShapeStyle =
            entries.firstOrNull { it.key == key } ?: ROUNDED
    }
}
