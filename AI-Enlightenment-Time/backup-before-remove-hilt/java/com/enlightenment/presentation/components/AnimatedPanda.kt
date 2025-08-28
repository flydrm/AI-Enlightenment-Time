package com.enlightenment.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// PandaMood已在单独文件中定义

@Composable
fun AnimatedPanda(
    mood: PandaMood = PandaMood.HAPPY,
    size: Dp = 100.dp,
    modifier: Modifier = Modifier
) {
    // 简化版熊猫动画组件
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // 使用emoji作为占位符
        androidx.compose.material3.Text(
            text = when (mood) {
                PandaMood.HAPPY -> "🐼"
                PandaMood.EXCITED -> "🎉"
                PandaMood.CURIOUS -> "🤔"
                PandaMood.SLEEPY -> "😴"
                PandaMood.PROUD -> "🌟"
            },
            style = MaterialTheme.typography.displayLarge
        )
    }
}