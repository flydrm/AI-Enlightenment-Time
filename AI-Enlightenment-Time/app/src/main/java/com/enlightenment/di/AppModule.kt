package com.enlightenment.di

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
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
