package com.enlightenment.domain.model

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi


data class Chapter(
    val id: String,
    val title: String,
    val content: String,
    val imageUrl: String? = null
)
