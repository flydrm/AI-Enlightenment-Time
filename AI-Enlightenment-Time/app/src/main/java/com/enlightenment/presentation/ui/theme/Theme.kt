package com.enlightenment.presentation.ui.theme

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat



private val LightColorScheme = lightColorScheme(
    primary = PrimaryRed,
    onPrimary = Color.White,
    primaryContainer = LightRed,
    onPrimaryContainer = DarkRed,
    
    secondary = SkyBlue,
    onSecondary = Color.White,
    secondaryContainer = LightBlue,
    onSecondaryContainer = Color(0xFF005885),
    
    tertiary = GrassGreen,
    onTertiary = Color.White,
    tertiaryContainer = LightGreen,
    onTertiaryContainer = Color(0xFF33691E),
    
    background = CreamWhite,
    onBackground = WoodBrown,
    
    surface = Color.White,
    onSurface = WoodBrown,
    surfaceVariant = CloudGray,
    onSurfaceVariant = TextGray,
    
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFCDD2),
    onErrorContainer = Color(0xFF5F0A0A),
    
    outline = LightBrown,
    outlineVariant = Color(0xFFEFEBE9),
    
    scrim = Color.Black.copy(alpha = 0.32f)
)
@Composable
fun EnlightenmentTheme(
    darkTheme: Boolean = false, // Children's app typically doesn't need dark theme
    content: @Composable () -> Unit
) {
    // For children's app, we always use light theme
    val colorScheme = LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = ChildFriendlyTypography,
        shapes = RoundedShapes,
        content = content
    )
}
