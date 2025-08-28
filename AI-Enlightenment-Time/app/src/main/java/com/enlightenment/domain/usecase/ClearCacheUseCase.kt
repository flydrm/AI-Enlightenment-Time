package com.enlightenment.domain.usecase

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi


class ClearCacheUseCase {
    suspend fun clearCache(): Boolean {
        // 清理缓存逻辑
        return true
    }
}
