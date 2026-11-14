package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard

@Composable
fun MinecraftSkinCanvas(
    bitmap: Bitmap?,
    showBack: Boolean = false,
    isSlim: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val imageBitmap = remember(bitmap) { bitmap?.asImageBitmap() }

    Box(
        modifier = modifier
            .clip(androidx.compose.material3.MaterialTheme.shapes.medium)
            .background(ObsidianCard)
            .border(1.dp, ObsidianBorder, androidx.compose.material3.MaterialTheme.shapes.medium)
            .padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (imageBitmap != null) {
            Canvas(modifier = Modifier.size(width = 128.dp, height = 192.dp)) {
                val u = size.width / 16f
                val v = size.height / 32f
                val armW = if (isSlim) 3f else 4f
                val armSrcW = if (isSlim) 3 else 4

                fun drawRegion(srcX: Int, srcY: Int, srcW: Int, srcH: Int, dstX: Float, dstY: Float, dstW: Float, dstH: Float) {
                    drawImage(
                        image = imageBitmap,
                        srcOffset = IntOffset(srcX, srcY),
                        srcSize = IntSize(srcW, srcH),
                        dstOffset = IntOffset((dstX * u).toInt(), (dstY * v).toInt()),
                        dstSize = IntSize((dstW * u).toInt(), (dstH * v).toInt()),
                        filterQuality = FilterQuality.None,
                    )
                }

                if (!showBack) {
                    // Head + Hat overlay
                    drawRegion(8, 8, 8, 8, 4f, 0f, 8f, 8f)
                    drawRegion(40, 8, 8, 8, 4f, 0f, 8f, 8f)
                    // Torso
                    drawRegion(20, 20, 8, 12, 4f, 8f, 8f, 12f)
                    drawRegion(20, 36, 8, 12, 4f, 8f, 8f, 12f)
                    // Right Arm & Left Arm
                    drawRegion(44, 20, armSrcW, 12, 4f - armW, 8f, armW, 12f)
                    drawRegion(36, 52, armSrcW, 12, 12f, 8f, armW, 12f)
                    // Right Leg & Left Leg
                    drawRegion(4, 20, 4, 12, 4f, 20f, 4f, 12f)
                    drawRegion(20, 52, 4, 12, 8f, 20f, 4f, 12f)
                } else {
                    // Back of Head + Hat
                    drawRegion(24, 8, 8, 8, 4f, 0f, 8f, 8f)
                    drawRegion(56, 8, 8, 8, 4f, 0f, 8f, 8f)
                    // Back of Torso
                    drawRegion(32, 20, 8, 12, 4f, 8f, 8f, 12f)
                    // Back of Arms
                    drawRegion(44 + armSrcW * 2, 20, armSrcW, 12, 12f, 8f, armW, 12f)
                    drawRegion(36 + armSrcW * 2, 52, armSrcW, 12, 4f - armW, 8f, armW, 12f)
                    // Back of Legs
                    drawRegion(12, 20, 4, 12, 8f, 20f, 4f, 12f)
                    drawRegion(28, 52, 4, 12, 4f, 20f, 4f, 12f)
                }
            }
        }
    }
}
