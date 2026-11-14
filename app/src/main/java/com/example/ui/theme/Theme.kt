package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun MichichModsTheme(
    colorTheme: AppColorTheme = AppColorTheme.ORANGE,
    shapeStyle: AppShapeStyle = AppShapeStyle.ROUNDED,
    content: @Composable () -> Unit,
) {
    val colorScheme = darkColorScheme(
        primary = colorTheme.primary,
        onPrimary = ObsidianBg,
        primaryContainer = colorTheme.containerTint,
        onPrimaryContainer = colorTheme.primary,
        secondary = colorTheme.secondary,
        onSecondary = ObsidianBg,
        secondaryContainer = ObsidianElevated,
        onSecondaryContainer = colorTheme.secondary,
        tertiary = EmeraldAccent,
        onTertiary = ObsidianBg,
        background = ObsidianBg,
        onBackground = TextPrimary,
        surface = ObsidianSurface,
        onSurface = TextPrimary,
        surfaceVariant = ObsidianCard,
        onSurfaceVariant = TextSecondary,
        outline = ObsidianBorder,
        error = VolcanicRed,
        onError = TextPrimary,
    )

    val shapes = Shapes(
        small = RoundedCornerShape(shapeStyle.smallDp.dp),
        medium = RoundedCornerShape(shapeStyle.mediumDp.dp),
        large = RoundedCornerShape(shapeStyle.largeDp.dp),
        extraLarge = RoundedCornerShape((shapeStyle.largeDp + 4).dp),
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LauncherTypography,
        shapes = shapes,
        content = content,
    )
}
