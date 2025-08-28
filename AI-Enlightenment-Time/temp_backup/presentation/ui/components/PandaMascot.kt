package com.enlightenment.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp
import com.enlightenment.presentation.ui.theme.PrimaryRed
import com.enlightenment.presentation.ui.theme.SoftRed

enum class PandaMood {
    HAPPY,
    EXCITED,
    THINKING,
    SLEEPING,
    CHEERING
}

@Composable
fun PandaMascot(
    mood: PandaMood = PandaMood.HAPPY,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "panda_animation")
    
    // Breathing animation
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )
    
    // Swaying animation
    val swayRotation by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "swaying"
    )
    
    Canvas(
        modifier = modifier.size(60.dp)
    ) {
        scale(breathingScale) {
            rotate(swayRotation) {
                drawPanda(mood)
            }
        }
    }
}

private fun DrawScope.drawPanda(mood: PandaMood) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val headRadius = size.minDimension / 3
    
    // Body
    drawCircle(
        color = PrimaryRed,
        radius = headRadius * 1.2f,
        center = Offset(centerX, centerY + headRadius * 0.8f)
    )
    
    // Head
    drawCircle(
        color = PrimaryRed,
        radius = headRadius,
        center = Offset(centerX, centerY)
    )
    
    // Ears
    drawCircle(
        color = SoftRed,
        radius = headRadius * 0.4f,
        center = Offset(centerX - headRadius * 0.7f, centerY - headRadius * 0.7f)
    )
    drawCircle(
        color = SoftRed,
        radius = headRadius * 0.4f,
        center = Offset(centerX + headRadius * 0.7f, centerY - headRadius * 0.7f)
    )
    
    // Eyes based on mood
    val eyeRadius = headRadius * 0.15f
    val eyeY = centerY - headRadius * 0.1f
    
    when (mood) {
        PandaMood.HAPPY, PandaMood.EXCITED -> {
            // Open eyes
            drawCircle(
                color = Color.Black,
                radius = eyeRadius,
                center = Offset(centerX - headRadius * 0.3f, eyeY)
            )
            drawCircle(
                color = Color.Black,
                radius = eyeRadius,
                center = Offset(centerX + headRadius * 0.3f, eyeY)
            )
        }
        PandaMood.SLEEPING -> {
            // Closed eyes (lines)
            val eyePath = Path().apply {
                moveTo(centerX - headRadius * 0.4f, eyeY)
                lineTo(centerX - headRadius * 0.2f, eyeY)
                moveTo(centerX + headRadius * 0.2f, eyeY)
                lineTo(centerX + headRadius * 0.4f, eyeY)
            }
            drawPath(eyePath, Color.Black, style = Fill)
        }
        else -> {
            // Default eyes
            drawCircle(
                color = Color.Black,
                radius = eyeRadius,
                center = Offset(centerX - headRadius * 0.3f, eyeY)
            )
            drawCircle(
                color = Color.Black,
                radius = eyeRadius,
                center = Offset(centerX + headRadius * 0.3f, eyeY)
            )
        }
    }
    
    // Nose
    drawCircle(
        color = Color.Black,
        radius = headRadius * 0.08f,
        center = Offset(centerX, centerY + headRadius * 0.1f)
    )
    
    // Mouth based on mood
    val mouthPath = Path().apply {
        when (mood) {
            PandaMood.HAPPY, PandaMood.EXCITED -> {
                // Smile
                moveTo(centerX - headRadius * 0.2f, centerY + headRadius * 0.3f)
                quadraticBezierTo(
                    centerX, centerY + headRadius * 0.4f,
                    centerX + headRadius * 0.2f, centerY + headRadius * 0.3f
                )
            }
            PandaMood.THINKING -> {
                // Straight mouth
                moveTo(centerX - headRadius * 0.15f, centerY + headRadius * 0.3f)
                lineTo(centerX + headRadius * 0.15f, centerY + headRadius * 0.3f)
            }
            else -> {
                // Small smile
                moveTo(centerX - headRadius * 0.15f, centerY + headRadius * 0.3f)
                quadraticBezierTo(
                    centerX, centerY + headRadius * 0.35f,
                    centerX + headRadius * 0.15f, centerY + headRadius * 0.3f
                )
            }
        }
    }
    drawPath(mouthPath, Color.Black, style = Fill)
}