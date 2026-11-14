package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.data.local.SavedSkinEntity
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.NetherGold
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.VolcanicRed

private enum class SkinZone(val label: String, val x0: Int, val y0: Int, val w: Int, val h: Int) {
    HEAD("Head", 8, 8, 8, 8),
    TORSO("Torso", 20, 20, 8, 12),
    RIGHT_ARM("R-Arm", 44, 20, 4, 12),
    RIGHT_LEG("R-Leg", 4, 20, 4, 12),
}

@Composable
fun SkinPixelEditorDialog(
    skin: SavedSkinEntity,
    initialBitmap: Bitmap,
    onSave: (Bitmap) -> Unit,
    onDismiss: () -> Unit,
) {
    val editableBitmap = remember(skin.id) { initialBitmap.copy(Bitmap.Config.ARGB_8888, true) }
    var revision by remember { mutableIntStateOf(0) }
    var selectedZone by remember { mutableStateOf(SkinZone.HEAD) }
    val palette = remember {
        listOf(
            NetherGold,
            VolcanicRed,
            EmeraldAccent,
            Color(0xFF14B8A6),
            Color(0xFFA855F7),
            Color(0xFF1A1D28),
            Color(0xFFEBC3A5),
            Color(0xFFFFFFFF),
        )
    }
    var selectedColor by remember { mutableStateOf(NetherGold) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurface,
        title = { Text("Pixel Editor • ${skin.name}") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    SkinZone.entries.forEach { zone ->
                        FilterChip(
                            selected = selectedZone == zone,
                            onClick = { selectedZone = zone },
                            label = { Text(zone.label, style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }

                // Pixel grid canvas for selected zone
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianBg)
                        .border(1.dp, ObsidianBorder, RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(180.dp)
                            .pointerInput(selectedZone, selectedColor) {
                                detectTapGestures { offset ->
                                    val cellW = size.width / selectedZone.w.toFloat()
                                    val cellH = size.height / selectedZone.h.toFloat()
                                    val px = (offset.x / cellW).toInt().coerceIn(0, selectedZone.w - 1)
                                    val py = (offset.y / cellH).toInt().coerceIn(0, selectedZone.h - 1)
                                    editableBitmap.setPixel(
                                        selectedZone.x0 + px,
                                        selectedZone.y0 + py,
                                        selectedColor.toArgb(),
                                    )
                                    revision++
                                }
                            },
                    ) {
                        @Suppress("UNUSED_EXPRESSION")
                        revision
                        val cellW = size.width / selectedZone.w.toFloat()
                        val cellH = size.height / selectedZone.h.toFloat()
                        for (y in 0 until selectedZone.h) {
                            for (x in 0 until selectedZone.w) {
                                val argb = editableBitmap.getPixel(selectedZone.x0 + x, selectedZone.y0 + y)
                                drawRect(
                                    color = Color(argb),
                                    topLeft = Offset(x * cellW, y * cellH),
                                    size = Size(cellW, cellH),
                                )
                                drawRect(
                                    color = ObsidianBorder.copy(alpha = 0.5f),
                                    topLeft = Offset(x * cellW, y * cellH),
                                    size = Size(cellW, cellH),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f),
                                )
                            }
                        }
                    }
                }

                // Color Palette
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    palette.forEach { swatch ->
                        val active = swatch == selectedColor
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(swatch)
                                .border(
                                    width = if (active) 2.5.dp else 1.dp,
                                    color = if (active) Color.White else ObsidianBorder,
                                    shape = CircleShape,
                                )
                                .clickable { selectedColor = swatch },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(editableBitmap) },
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = ObsidianBg,
                ),
            ) {
                Text("Save Skin")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
