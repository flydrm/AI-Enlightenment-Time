package com.enlightenment.presentation.camera

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import com.enlightenment.ai.model.RecognitionResult as AIRecognitionResult



/**
 * 相机识别结果
 */
data class RecognitionResult(
    val recognizedObjects: List<AIRecognitionResult>,
    val childFriendlyDescription: String,
    val timestamp: Long = System.currentTimeMillis()
)
