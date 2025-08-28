package com.enlightenment.di

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import android.content.Context
import com.enlightenment.data.local.database.AppDatabase
import com.enlightenment.security.AuditLogger
import com.enlightenment.security.SecureStorage
import com.enlightenment.security.SecurityManager



/**
 * 安全相关的依赖注入模块
 */
object SecurityModule {
    
    
    
    fun provideSecureStorage(
        context: Context
    ): SecureStorage {
        return SecureStorage(context)
    }
    
    
    
    fun provideAuditLogger(
        context: Context,
        database: AppDatabase,
        secureStorage: SecureStorage
    ): AuditLogger {
        return AuditLogger(context, database, secureStorage)
    }
}
