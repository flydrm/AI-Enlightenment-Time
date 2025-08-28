package com.enlightenment.presentation.components.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import kotlin.math.*
import kotlin.random.Random
import kotlinx.coroutines.delay



/**
 * 粒子效果组件
 */
@Composable
fun ParticleEffect(
    modifier: Modifier = Modifier,
    particleCount: Int = 50,
    particleColor: Color = MaterialTheme.colorScheme.primary,
    animationDuration: Int = 3000
) {
    val particles = remember {
        List(particleCount) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                vx = Random.nextFloat() * 0.002f - 0.001f,
                vy = Random.nextFloat() * 0.002f - 0.001f,
                size = Random.nextFloat() * 4f + 2f,
                alpha = Random.nextFloat() * 0.5f + 0.5f
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition()
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Canvas(modifier = modifier) {
        particles.forEach { particle ->
            updateParticle(particle, animationProgress)
            drawParticle(particle, particleColor, size)
        }
    }
}
private data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val size: Float,
    var alpha: Float
)
private fun updateParticle(particle: Particle, progress: Float) {
    particle.x += particle.vx
    particle.y += particle.vy
    
    // 边界检测
    if (particle.x < 0 || particle.x > 1) particle.vx *= -1
    if (particle.y < 0 || particle.y > 1) particle.vy *= -1
    
    // 更新透明度
    particle.alpha = (sin(progress * PI * 2).toFloat() + 1) / 2 * 0.5f + 0.5f
}
private fun DrawScope.drawParticle(
    particle: Particle,
    color: Color,
    canvasSize: androidx.compose.ui.geometry.Size
) {
    drawCircle(
        color = color.copy(alpha = particle.alpha),
        radius = particle.size.dp.toPx(),
        center = Offset(
            x = particle.x * canvasSize.width,
            y = particle.y * canvasSize.height
        )
    )
}
/**
 * 波浪动画效果
 */
@Composable
fun WaveAnimation(
    modifier: Modifier = Modifier,
    waveColor: Color = MaterialTheme.colorScheme.primary,
    waveHeight: Dp = 50.dp,
    animationDuration: Int = 2000
) {
    val infiniteTransition = rememberInfiniteTransition()
    val waveProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Canvas(modifier = modifier) {
        val wavePath = Path()
        val waveLength = size.width
        val amplitude = waveHeight.toPx()
        
        wavePath.moveTo(0f, size.height / 2)
        
        for (x in 0..size.width.toInt()) {
            val y = size.height / 2 + amplitude * sin(
                2 * PI * (x / waveLength + waveProgress)
            ).toFloat()
            wavePath.lineTo(x.toFloat(), y)
        }
        
        wavePath.lineTo(size.width, size.height)
        wavePath.lineTo(0f, size.height)
        wavePath.close()
        
        drawPath(
            path = wavePath,
            color = waveColor.copy(alpha = 0.3f)
        )
    }
}
/**
 * 脉冲动画效果
 */
@Composable
fun PulseAnimation(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = modifier.scale(scale),
        content = content
    )
}
/**
 * 摇摆动画效果
 */
@Composable
fun ShakeAnimation(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (enabled) 0f else 0f,
        animationSpec = if (enabled) {
            infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 100
                    0f at 0
                    -5f at 25
                    5f at 75
                    0f at 100
                },
                repeatMode = RepeatMode.Restart
            )
        } else {
            spring()
        }
    )
    
    Box(
        modifier = modifier.rotate(rotation),
        content = content
    )
}
/**
 * 彩虹渐变动画
 */
@Composable
fun RainbowAnimation(
    modifier: Modifier = Modifier,
    animationDuration: Int = 3000
) {
    val infiniteTransition = rememberInfiniteTransition()
    val hue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Box(
        modifier = modifier.background(
            Brush.sweepGradient(
                colors = listOf(
                    Color.hsv(hue, 0.8f, 1f),
                    Color.hsv((hue + 60) % 360, 0.8f, 1f),
                    Color.hsv((hue + 120) % 360, 0.8f, 1f),
                    Color.hsv((hue + 180) % 360, 0.8f, 1f),
                    Color.hsv((hue + 240) % 360, 0.8f, 1f),
                    Color.hsv((hue + 300) % 360, 0.8f, 1f),
                    Color.hsv(hue, 0.8f, 1f)
                )
            )
        )
    )
}
/**
 * 弹跳加载动画
 */
@Composable
fun BouncingDotsAnimation(
    modifier: Modifier = Modifier,
    dotCount: Int = 3,
    dotSize: Dp = 12.dp,
    dotColor: Color = MaterialTheme.colorScheme.primary,
    animationDuration: Int = 600
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(dotSize / 2)
    ) {
        repeat(dotCount) { index ->
            val infiniteTransition = rememberInfiniteTransition()
            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -dotSize.value,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = animationDuration,
                        delayMillis = index * 150,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                )
            )
            
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .offset(y = offsetY.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}
/**
 * 打字机效果文本
 */
@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    typeSpeed: Long = 50L,
    onComplete: () -> Unit = {}
) {
    var displayedText by remember { mutableStateOf("") }
    
    LaunchedEffect(text) {
        displayedText = ""
        text.forEachIndexed { index, _ ->
            displayedText = text.substring(0, index + 1)
            delay(typeSpeed)
        }
        onComplete()
    }
    
    androidx.compose.material3.Text(
        text = displayedText,
        modifier = modifier
    )
}
/**
 * 翻转卡片动画
 */
@Composable
fun FlipCard(
    modifier: Modifier = Modifier,
    isFlipped: Boolean,
    frontContent: @Composable () -> Unit,
    backContent: @Composable () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing)
    )
    
    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
    ) {
        if (rotation <= 90f) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                frontContent()
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = 180f
                    }
            ) {
                backContent()
            }
        }
    }
}
/**
 * 涟漪扩散效果
 */
@Composable
fun RippleEffect(
    modifier: Modifier = Modifier,
    rippleColor: Color = MaterialTheme.colorScheme.primary,
    animationDuration: Int = 1000
) {
    val infiniteTransition = rememberInfiniteTransition()
    
    Box(modifier = modifier) {
        repeat(3) { index ->
            val delay = index * 300
            val scale by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = animationDuration,
                        delayMillis = delay,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Restart
                )
            )
            
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = animationDuration,
                        delayMillis = delay,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                )
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scale)
                    .clip(CircleShape)
                    .background(rippleColor.copy(alpha = alpha))
            )
        }
    }
}
/**
 * 浮动气泡效果
 */
@Composable
fun FloatingBubbles(
    modifier: Modifier = Modifier,
    bubbleCount: Int = 10,
    bubbleColor: Color = MaterialTheme.colorScheme.primary
) {
    val bubbles = remember {
        List(bubbleCount) {
            Bubble(
                x = Random.nextFloat(),
                y = Random.nextFloat() + 1f,
                size = Random.nextFloat() * 20f + 10f,
                speed = Random.nextFloat() * 0.002f + 0.001f
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition()
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Canvas(modifier = modifier) {
        bubbles.forEach { bubble ->
            updateBubble(bubble, animationProgress)
            drawBubble(bubble, bubbleColor, size)
        }
    }
}
private data class Bubble(
    val x: Float,
    var y: Float,
    val size: Float,
    val speed: Float
)
private fun updateBubble(bubble: Bubble, progress: Float) {
    bubble.y -= bubble.speed
    if (bubble.y < -0.1f) {
        bubble.y = 1.1f
    }
}
private fun DrawScope.drawBubble(
    bubble: Bubble,
    color: Color,
    canvasSize: androidx.compose.ui.geometry.Size
) {
    drawCircle(
        color = color.copy(alpha = 0.3f),
        radius = bubble.size.dp.toPx(),
        center = Offset(
            x = bubble.x * canvasSize.width,
            y = bubble.y * canvasSize.height
        )
    )
}
