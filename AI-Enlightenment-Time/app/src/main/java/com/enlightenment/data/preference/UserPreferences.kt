package com.enlightenment.data.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * 用户偏好设置管理器
 */

class UserPreferences constructor(
    private val context: Context
) {
    
    companion object {
        // 用户设置键
        private val CHILD_AGE_KEY = intPreferencesKey("child_age")
        private val CHILD_NAME_KEY = stringPreferencesKey("child_name")
        private val PARENTAL_CONTROL_ENABLED_KEY = booleanPreferencesKey("parental_control_enabled")
        private val PARENTAL_PIN_KEY = stringPreferencesKey("parental_pin")
        private val VOLUME_LEVEL_KEY = floatPreferencesKey("volume_level")
        private val NIGHT_MODE_ENABLED_KEY = booleanPreferencesKey("night_mode_enabled")
        private val AUTO_PLAY_ENABLED_KEY = booleanPreferencesKey("auto_play_enabled")
        private val CLOUD_SYNC_ENABLED_KEY = booleanPreferencesKey("cloud_sync_enabled")
        private val DAILY_TIME_LIMIT_KEY = intPreferencesKey("daily_time_limit_minutes")
        private val NOTIFICATION_ENABLED_KEY = booleanPreferencesKey("notification_enabled")
        
        // 默认值
        const val DEFAULT_CHILD_AGE = 5
        const val DEFAULT_VOLUME_LEVEL = 0.7f
        const val DEFAULT_DAILY_TIME_LIMIT = 15
    }
    
    /**
     * 获取儿童年龄
     */
    val childAge: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[CHILD_AGE_KEY] ?: DEFAULT_CHILD_AGE
        }
    
    /**
     * 设置儿童年龄
     */
    suspend fun setChildAge(age: Int) {
        context.dataStore.edit { preferences ->
            preferences[CHILD_AGE_KEY] = age.coerceIn(3, 6)
        }
    }
    
    /**
     * 获取儿童姓名
     */
    val childName: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[CHILD_NAME_KEY]
        }
    
    /**
     * 设置儿童姓名
     */
    suspend fun setChildName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[CHILD_NAME_KEY] = name
        }
    }
    
    /**
     * 获取家长控制是否启用
     */
    val isParentalControlEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PARENTAL_CONTROL_ENABLED_KEY] ?: true
        }
    
    /**
     * 设置家长控制
     */
    suspend fun setParentalControlEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PARENTAL_CONTROL_ENABLED_KEY] = enabled
        }
    }
    
    /**
     * 获取音量级别
     */
    val volumeLevel: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[VOLUME_LEVEL_KEY] ?: DEFAULT_VOLUME_LEVEL
        }
    
    /**
     * 设置音量级别
     */
    suspend fun setVolumeLevel(level: Float) {
        context.dataStore.edit { preferences ->
            preferences[VOLUME_LEVEL_KEY] = level.coerceIn(0f, 1f)
        }
    }
    
    /**
     * 获取夜间模式状态
     */
    val isNightModeEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[NIGHT_MODE_ENABLED_KEY] ?: false
        }
    
    /**
     * 设置夜间模式
     */
    suspend fun setNightModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NIGHT_MODE_ENABLED_KEY] = enabled
        }
    }
    
    /**
     * 获取自动播放状态
     */
    val isAutoPlayEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[AUTO_PLAY_ENABLED_KEY] ?: true
        }
    
    /**
     * 设置自动播放
     */
    suspend fun setAutoPlayEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_PLAY_ENABLED_KEY] = enabled
        }
    }
    
    /**
     * 获取云同步状态
     */
    val isCloudSyncEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[CLOUD_SYNC_ENABLED_KEY] ?: false
        }
    
    /**
     * 设置云同步
     */
    suspend fun setCloudSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CLOUD_SYNC_ENABLED_KEY] = enabled
        }
    }
    
    /**
     * 获取每日时间限制（分钟）
     */
    val dailyTimeLimit: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[DAILY_TIME_LIMIT_KEY] ?: DEFAULT_DAILY_TIME_LIMIT
        }
    
    /**
     * 设置每日时间限制
     */
    suspend fun setDailyTimeLimit(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[DAILY_TIME_LIMIT_KEY] = minutes.coerceIn(5, 60)
        }
    }
    
    /**
     * 清除所有设置
     */
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}