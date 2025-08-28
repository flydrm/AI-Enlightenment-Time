package com.enlightenment.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.enlightenment.presentation.navigation.AppNavigation
import com.enlightenment.presentation.ui.theme.EnlightenmentTheme

/**
 * 主Activity - 不使用Hilt
 */
class MainActivityNoHilt : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 设置沉浸式状态栏
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            EnlightenmentTheme {
                MainContent()
            }
        }
    }
}

@Composable
private fun MainContent() {
    val navController = rememberNavController()
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AppNavigation(navController = navController)
    }
}