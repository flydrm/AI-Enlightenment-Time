package com.enlightenment.di

import android.content.Context
import com.enlightenment.data.local.database.AppDatabase
import com.enlightenment.security.AuditLogger
import com.enlightenment.security.SecureStorage
import com.enlightenment.security.SecurityManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 安全相关的依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {
    
    @Provides
    @Singleton
    fun provideSecureStorage(
        @ApplicationContext context: Context
    ): SecureStorage {
        return SecureStorage(context)
    }
    
    @Provides
    @Singleton
    fun provideAuditLogger(
        @ApplicationContext context: Context,
        database: AppDatabase,
        secureStorage: SecureStorage
    ): AuditLogger {
        return AuditLogger(context, database, secureStorage)
    }
}