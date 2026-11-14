package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
) {
    LaunchedEffect(Unit) {
        delay(1750L)
        onFinished()
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val infiniteTransition = rememberInfiniteTransition(label = "theme_pulse")
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
        ),
        label = "ring_rotation",
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_scale",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        secondaryColor.copy(alpha = 0.18f),
                        ObsidianBg,
                        primaryColor.copy(alpha = 0.08f),
                    )
                )
            )
            .clickable(onClick = onFinished)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier.size(156.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(152.dp)
                        .rotate(ringRotation)
                        .clip(CircleShape)
                        .border(
                            width = 3.dp,
                            brush = Brush.sweepGradient(
                                listOf(
                                    primaryColor,
                                    secondaryColor,
                                    primaryColor.copy(alpha = 0.1f),
                                    secondaryColor,
                                    primaryColor,
                                )
                            ),
                            shape = CircleShape,
                        ),
                )
                Image(
                    painter = painterResource(id = R.drawable.app_icon_michich),
                    contentDescription = "Michich Mods Hub Emblem",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(124.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .border(2.dp, primaryColor.copy(alpha = 0.7f), CircleShape),
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "MICHICH MODS HUB",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                letterSpacing = 1.5.sp,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "FORGING MODS, PACKS, SHADERS & SKINS",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                letterSpacing = 1.2.sp,
            )

            Spacer(Modifier.height(28.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .width(200.dp)
                    .height(4.dp)
                    .clip(MaterialTheme.shapes.small),
                color = primaryColor,
                trackColor = ObsidianCard,
            )

            Spacer(Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.alpha(0.8f),
            ) {
                Text("MODRINTH", style = MaterialTheme.typography.labelSmall, color = primaryColor)
                Text("•", style = MaterialTheme.typography.labelSmall, color = secondaryColor)
                Text("MCPEDL", style = MaterialTheme.typography.labelSmall, color = primaryColor)
                Text("•", style = MaterialTheme.typography.labelSmall, color = secondaryColor)
                Text("SKIN STUDIO", style = MaterialTheme.typography.labelSmall, color = primaryColor)
            }
        }
    }
}
