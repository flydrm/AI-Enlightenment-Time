package com.enlightenment.presentation.ui.responsive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp



/**
 * 窗口大小类别
 * 基于Material Design的响应式布局指南
 */
enum class WindowSizeClass {
    COMPACT,    // 手机竖屏 (< 600dp)
    MEDIUM,     // 平板竖屏或大手机横屏 (600dp - 840dp)
    EXPANDED    // 平板横屏或桌面 (> 840dp)
}
/**
 * 获取当前窗口大小类别
 */
@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    return remember(screenWidth) {
        getWindowSizeClass(screenWidth)
    }
}
/**
 * 根据宽度获取窗口大小类别
 */
fun getWindowSizeClass(width: Dp): WindowSizeClass {
    return when {
        width < 600.dp -> WindowSizeClass.COMPACT
        width < 840.dp -> WindowSizeClass.MEDIUM
        else -> WindowSizeClass.EXPANDED
    }
}
/**
 * 设备方向
 */
enum class DeviceOrientation {
    PORTRAIT,
    LANDSCAPE
}
/**
 * 获取设备方向
 */
@Composable
fun rememberDeviceOrientation(): DeviceOrientation {
    val configuration = LocalConfiguration.current
    return remember(configuration.orientation) {
        when (configuration.orientation) {
            android.content.res.Configuration.ORIENTATION_LANDSCAPE -> DeviceOrientation.LANDSCAPE
            else -> DeviceOrientation.PORTRAIT
        }
    }
}
/**
 * 响应式尺寸数据类
 */
data class ResponsiveSizes(
    val windowSizeClass: WindowSizeClass,
    val orientation: DeviceOrientation,
    val screenWidthDp: Dp,
    val screenHeightDp: Dp
)
/**
 * 获取响应式尺寸信息
 */
@Composable
fun rememberResponsiveSizes(): ResponsiveSizes {
    val configuration = LocalConfiguration.current
    val windowSizeClass = rememberWindowSizeClass()
    val orientation = rememberDeviceOrientation()
    
    return remember(configuration) {
        ResponsiveSizes(
            windowSizeClass = windowSizeClass,
            orientation = orientation,
            screenWidthDp = configuration.screenWidthDp.dp,
            screenHeightDp = configuration.screenHeightDp.dp
        )
    }
}
/**
 * 响应式内边距
 */
object ResponsivePadding {
    @Composable
    fun horizontal(): Dp {
        return when (rememberWindowSizeClass()) {
            WindowSizeClass.COMPACT -> 16.dp
            WindowSizeClass.MEDIUM -> 32.dp
            WindowSizeClass.EXPANDED -> 64.dp
        }
    }
    
    @Composable
    fun vertical(): Dp {
        return when (rememberWindowSizeClass()) {
            WindowSizeClass.COMPACT -> 16.dp
            WindowSizeClass.MEDIUM -> 24.dp
            WindowSizeClass.EXPANDED -> 32.dp
        }
    }
}
/**
 * 响应式网格列数
 */
object ResponsiveGrid {
    @Composable
    fun columns(): Int {
        val windowSizeClass = rememberWindowSizeClass()
        val orientation = rememberDeviceOrientation()
        
        return when (windowSizeClass) {
            WindowSizeClass.COMPACT -> when (orientation) {
                DeviceOrientation.PORTRAIT -> 2
                DeviceOrientation.LANDSCAPE -> 3
            }
            WindowSizeClass.MEDIUM -> when (orientation) {
                DeviceOrientation.PORTRAIT -> 3
                DeviceOrientation.LANDSCAPE -> 4
            }
            WindowSizeClass.EXPANDED -> when (orientation) {
                DeviceOrientation.PORTRAIT -> 4
                DeviceOrientation.LANDSCAPE -> 6
            }
        }
    }
}
