package com.enlightenment.domain.repository

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import kotlinx.coroutines.flow.Flow



interface SettingsRepository {
    fun getSettings(): Flow<Settings>
    suspend fun updateSettings(settings: Settings)
}
data class Settings(
    val isDarkMode: Boolean = false,
    val fontSize: Float = 1.0f,
    val isVoiceEnabled: Boolean = true,
    val notificationEnabled: Boolean = true
)
