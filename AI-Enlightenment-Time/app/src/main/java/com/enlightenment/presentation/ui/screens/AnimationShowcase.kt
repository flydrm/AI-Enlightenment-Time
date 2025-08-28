package com.enlightenment.presentation.ui.screens

@file:OptIn(ExperimentalMaterial3Api::class)

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.enlightenment.presentation.components.animations.*
import com.enlightenment.presentation.components.EnhancedAnimatedPanda
import com.enlightenment.presentation.components.PandaMood





/**
 * 动画效果展示页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimationShowcaseScreen() {
    var selectedAnimation by remember { mutableStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("动画效果展示") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 增强版小熊猫动画
            item {
                AnimationCard(title = "增强版小熊猫") {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            EnhancedAnimatedPanda(
                                mood = PandaMood.HAPPY,
                                speech = "我很开心！"
                            )
                            Text("开心", style = MaterialTheme.typography.typography.bodySmall)
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            EnhancedAnimatedPanda(
                                mood = PandaMood.EXCITED,
                                speech = "太棒了！"
                            )
                            Text("兴奋", style = MaterialTheme.typography.typography.bodySmall)
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            EnhancedAnimatedPanda(
                                mood = PandaMood.THINKING,
                                speech = "让我想想..."
                            )
                            Text("思考", style = MaterialTheme.typography.typography.bodySmall)
                        }
                    }
                }
            }
            
            // 粒子效果
            item {
                AnimationCard(title = "粒子效果") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        ParticleEffect(
                            modifier = Modifier.fillMaxSize(),
                            particleCount = 30
                        )
                    }
                }
            }
            
            // 波浪动画
            item {
                AnimationCard(title = "波浪动画") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        WaveAnimation(
                            modifier = Modifier.fillMaxSize(),
                            waveColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // 脉冲动画
            item {
                AnimationCard(title = "脉冲动画") {
                    PulseAnimation(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { },
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Text("脉冲按钮")
                        }
                    }
                }
            }
            
            // 彩虹渐变
            item {
                AnimationCard(title = "彩虹渐变") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        RainbowAnimation(
                            modifier = Modifier.fillMaxSize()
                        )
                        Text(
                            "彩虹效果",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            
            // 弹跳加载动画
            item {
                AnimationCard(title = "弹跳加载") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        BouncingDotsAnimation()
                    }
                }
            }
            
            // 打字机效果
            item {
                AnimationCard(title = "打字机效果") {
                    var text by remember { mutableStateOf("") }
                    var key by remember { mutableStateOf(0) }
                    
                    Column {
                        TypewriterText(
                            text = "欢迎来到AI启蒙时光！这是一个充满乐趣的学习世界。",
                            key = key
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { 
                                key++
                            }
                        ) {
                            Text("重播")
                        }
                    }
                }
            }
            
            // 翻转卡片
            item {
                AnimationCard(title = "翻转卡片") {
                    var isFlipped by remember { mutableStateOf(false) }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FlipCard(
                            isFlipped = isFlipped,
                            frontContent = {
                                Card(
                                    modifier = Modifier.fillMaxSize(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                Icons.Default.TouchApp,
                                                contentDescription = null,
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Text("点击翻转")
                                        }
                                    }
                                }
                            },
                            backContent = {
                                Card(
                                    modifier = Modifier.fillMaxSize(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                Icons.Default.Celebration,
                                                contentDescription = null,
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Text("背面内容")
                                        }
                                    }
                                }
                            }
                        )
                        
                        Button(
                            onClick = { isFlipped = !isFlipped },
                            modifier = Modifier.align(Alignment.BottomCenter)
                        ) {
                            Text("翻转")
                        }
                    }
                }
            }
            
            // 涟漪效果
            item {
                AnimationCard(title = "涟漪扩散") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        RippleEffect(
                            modifier = Modifier.size(100.dp)
                        )
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            
            // 浮动气泡
            item {
                AnimationCard(title = "浮动气泡") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        FloatingBubbles(
                            modifier = Modifier.fillMaxSize(),
                            bubbleColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun AnimationCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            content()
        }
    }
}
