package com.enlightenment.presentation.ui.theme

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp



// Rounded shapes for child-friendly design
val RoundedShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)
// Custom shapes for specific components
val ButtonShape = RoundedCornerShape(32.dp)
val CardShape = RoundedCornerShape(24.dp)
val DialogShape = RoundedCornerShape(28.dp)
val CircleShape = RoundedCornerShape(50)
