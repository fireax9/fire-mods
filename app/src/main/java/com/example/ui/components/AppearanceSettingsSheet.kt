package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RoundedCorner
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppColorTheme
import com.example.ui.theme.AppShapeStyle
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsSheet(
    currentColor: AppColorTheme,
    currentShape: AppShapeStyle,
    onSelectColor: (AppColorTheme) -> Unit,
    onSelectShape: (AppShapeStyle) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // Header & Live Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                currentColor.primary.copy(alpha = 0.24f),
                                currentColor.secondary.copy(alpha = 0.14f),
                                ObsidianCard,
                            )
                        )
                    )
                    .border(1.dp, currentColor.primary.copy(alpha = 0.6f), MaterialTheme.shapes.large)
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Appearance & Style",
                        style = MaterialTheme.typography.titleLarge,
                        color = currentColor.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${currentColor.label} Theme • ${currentShape.label} Corners",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }

            // Color Selector (4 colors: Red, Orange, Teal, Purple)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Text("Theme Color", style = MaterialTheme.typography.titleMedium)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AppColorTheme.entries.forEach { option ->
                        val selected = option == currentColor
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(MaterialTheme.shapes.medium)
                                .background(if (selected) ObsidianElevated else ObsidianCard)
                                .border(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) option.primary else ObsidianBorder,
                                    shape = MaterialTheme.shapes.medium,
                                )
                                .clickable { onSelectColor(option) }
                                .padding(vertical = 12.dp, horizontal = 6.dp)
                                .testTag("theme_color_${option.key.lowercase()}"),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(option.primary, option.secondary))
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected ${option.label}",
                                        tint = ObsidianBg,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) option.primary else TextPrimary,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            )
                        }
                    }
                }
            }

            // Shape Selector (Blocky, Rounded, Pill)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.RoundedCorner,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Text("Corner Shape", style = MaterialTheme.typography.titleMedium)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AppShapeStyle.entries.forEach { style ->
                        val selected = style == currentShape
                        val previewShape = RoundedCornerShape(style.mediumDp.dp)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(previewShape)
                                .background(if (selected) ObsidianElevated else ObsidianCard)
                                .border(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) MaterialTheme.colorScheme.primary else ObsidianBorder,
                                    shape = previewShape,
                                )
                                .clickable { onSelectShape(style) }
                                .padding(vertical = 14.dp, horizontal = 8.dp)
                                .testTag("theme_shape_${style.key.lowercase()}"),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 36.dp, height = 20.dp)
                                    .clip(RoundedCornerShape(style.smallDp.dp))
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                        else ObsidianBg
                                    )
                                    .border(
                                        1.5.dp,
                                        if (selected) MaterialTheme.colorScheme.primary else TextSecondary,
                                        RoundedCornerShape(style.smallDp.dp),
                                    ),
                            )
                            Text(
                                text = style.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) MaterialTheme.colorScheme.primary else TextPrimary,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
        }
    }
}
