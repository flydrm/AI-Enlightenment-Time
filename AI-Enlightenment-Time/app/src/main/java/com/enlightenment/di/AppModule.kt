package com.enlightenment.di

import android.content.Context
import com.enlightenment.security.SecurityManager

/**
 * 应用程序级别的依赖注入模块
 */


object AppModule {
    
    
    
    fun provideSecurityManager(
        context: Context
    ): SecurityManager {
        return SecurityManager(context)
    }
}