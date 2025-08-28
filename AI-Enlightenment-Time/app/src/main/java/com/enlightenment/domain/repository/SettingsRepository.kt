package com.enlightenment.domain.repository

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
