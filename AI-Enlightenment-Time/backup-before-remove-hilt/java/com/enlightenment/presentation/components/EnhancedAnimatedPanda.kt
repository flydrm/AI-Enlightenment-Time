package com.enlightenment.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.*

/**
 * 增强版小熊猫动画组件
 */
@Composable
fun EnhancedAnimatedPanda(
    modifier: Modifier = Modifier,
    mood: PandaMood = PandaMood.HAPPY,
    isActive: Boolean = true,
    speech: String? = null,
    onTap: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition()
    
    // 身体摇摆动画
    val bodyRotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    // 眨眼动画
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay((3000..5000).random().toLong())
            isBlinking = true
            delay(150)
            isBlinking = false
        }
    }
    
    // 呼吸动画
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 对话气泡
        AnimatedVisibility(
            visible = speech != null,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            SpeechBubble(
                text = speech ?: "",
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // 小熊猫本体
        Canvas(
            modifier = Modifier
                .size(100.dp)
                .scale(if (isActive) breathScale else 1f)
                .rotate(if (isActive) bodyRotation else 0f)
        ) {
            drawPanda(
                mood = mood,
                isBlinking = isBlinking,
                isActive = isActive
            )
        }
    }
}

/**
 * 小熊猫心情
 */
// PandaMood已在单独文件中定义

/**
 * 绘制小熊猫
 */
private fun DrawScope.drawPanda(
    mood: PandaMood,
    isBlinking: Boolean,
    isActive: Boolean
) {
    val pandaColor = Color(0xFFD32F2F) // 红色
    val whiteColor = Color.White
    val blackColor = Color.Black
    
    // 身体
    drawCircle(
        color = pandaColor,
        radius = size.minDimension * 0.4f,
        center = center
    )
    
    // 耳朵
    val earRadius = size.minDimension * 0.15f
    drawCircle(
        color = pandaColor,
        radius = earRadius,
        center = Offset(
            x = center.x - size.width * 0.3f,
            y = center.y - size.height * 0.3f
        )
    )
    drawCircle(
        color = pandaColor,
        radius = earRadius,
        center = Offset(
            x = center.x + size.width * 0.3f,
            y = center.y - size.height * 0.3f
        )
    )
    
    // 眼睛
    val eyeRadius = size.minDimension * 0.08f
    val eyeY = center.y - size.height * 0.1f
    
    if (!isBlinking) {
        // 左眼
        drawCircle(
            color = blackColor,
            radius = eyeRadius * 1.5f,
            center = Offset(center.x - size.width * 0.15f, eyeY)
        )
        drawCircle(
            color = whiteColor,
            radius = eyeRadius,
            center = Offset(center.x - size.width * 0.15f, eyeY)
        )
        drawCircle(
            color = blackColor,
            radius = eyeRadius * 0.5f,
            center = Offset(center.x - size.width * 0.15f, eyeY)
        )
        
        // 右眼
        drawCircle(
            color = blackColor,
            radius = eyeRadius * 1.5f,
            center = Offset(center.x + size.width * 0.15f, eyeY)
        )
        drawCircle(
            color = whiteColor,
            radius = eyeRadius,
            center = Offset(center.x + size.width * 0.15f, eyeY)
        )
        drawCircle(
            color = blackColor,
            radius = eyeRadius * 0.5f,
            center = Offset(center.x + size.width * 0.15f, eyeY)
        )
        
        // 根据心情添加眼睛表情
        when (mood) {
            PandaMood.HAPPY -> {
                // 弯月眼
                drawArc(
                    color = pandaColor,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(
                        center.x - size.width * 0.25f,
                        eyeY - eyeRadius * 2
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        eyeRadius * 2,
                        eyeRadius * 2
                    )
                )
                drawArc(
                    color = pandaColor,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(
                        center.x + size.width * 0.05f,
                        eyeY - eyeRadius * 2
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        eyeRadius * 2,
                        eyeRadius * 2
                    )
                )
            }
            PandaMood.EXCITED -> {
                // 星星眼
                repeat(3) { i ->
                    val angle = i * 120f
                    val sparkleOffset = Offset(
                        center.x - size.width * 0.15f + cos(angle * PI / 180).toFloat() * eyeRadius,
                        eyeY + sin(angle * PI / 180).toFloat() * eyeRadius
                    )
                    drawCircle(
                        color = Color.Yellow,
                        radius = eyeRadius * 0.2f,
                        center = sparkleOffset
                    )
                }
            }
            else -> {}
        }
    } else {
        // 闭眼时画一条线
        drawLine(
            color = blackColor,
            start = Offset(center.x - size.width * 0.2f, eyeY),
            end = Offset(center.x - size.width * 0.1f, eyeY),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = blackColor,
            start = Offset(center.x + size.width * 0.1f, eyeY),
            end = Offset(center.x + size.width * 0.2f, eyeY),
            strokeWidth = 2.dp.toPx()
        )
    }
    
    // 鼻子
    drawCircle(
        color = blackColor,
        radius = size.minDimension * 0.03f,
        center = Offset(center.x, center.y)
    )
    
    // 嘴巴
    val mouthPath = Path().apply {
        when (mood) {
            PandaMood.HAPPY, PandaMood.EXCITED -> {
                // 微笑
                moveTo(center.x - size.width * 0.1f, center.y + size.height * 0.05f)
                quadraticBezierTo(
                    center.x, center.y + size.height * 0.15f,
                    center.x + size.width * 0.1f, center.y + size.height * 0.05f
                )
            }
            PandaMood.SURPRISED -> {
                // 惊讶的O型嘴
                addOval(
                    androidx.compose.ui.geometry.Rect(
                        offset = Offset(
                            center.x - size.width * 0.05f,
                            center.y + size.height * 0.05f
                        ),
                        size = androidx.compose.ui.geometry.Size(
                            size.width * 0.1f,
                            size.height * 0.1f
                        )
                    )
                )
            }
            else -> {
                // 普通嘴巴
                moveTo(center.x - size.width * 0.05f, center.y + size.height * 0.08f)
                lineTo(center.x + size.width * 0.05f, center.y + size.height * 0.08f)
            }
        }
    }
    
    drawPath(
        path = mouthPath,
        color = blackColor,
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
    )
    
    // 腮红（仅在开心或兴奋时显示）
    if (mood == PandaMood.HAPPY || mood == PandaMood.EXCITED) {
        drawCircle(
            color = Color(0xFFFF6B6B).copy(alpha = 0.3f),
            radius = size.minDimension * 0.08f,
            center = Offset(center.x - size.width * 0.25f, center.y + size.height * 0.05f)
        )
        drawCircle(
            color = Color(0xFFFF6B6B).copy(alpha = 0.3f),
            radius = size.minDimension * 0.08f,
            center = Offset(center.x + size.width * 0.25f, center.y + size.height * 0.05f)
        )
    }
    
    // 手臂（简单的小圆）
    drawCircle(
        color = pandaColor,
        radius = size.minDimension * 0.1f,
        center = Offset(
            center.x - size.width * 0.35f,
            center.y + size.height * 0.1f
        )
    )
    drawCircle(
        color = pandaColor,
        radius = size.minDimension * 0.1f,
        center = Offset(
            center.x + size.width * 0.35f,
            center.y + size.height * 0.1f
        )
    )
}

/**
 * 对话气泡
 */
@Composable
private fun SpeechBubble(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}