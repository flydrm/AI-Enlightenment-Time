package com.enlightenment.data.network.interceptor

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import okhttp3.Interceptor
import okhttp3.Response



/**
 * API密钥拦截器
 */
class ApiKeyInterceptor(
    private val apiKeyProvider: () -> String,
    private val headerName: String = "Authorization",
    private val headerPrefix: String = "Bearer"
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val apiKey = apiKeyProvider()
        
        // 如果没有API密钥，直接返回原始请求
        if (apiKey.isEmpty()) {
            return chain.proceed(originalRequest)
        }
        
        // 添加API密钥到请求头
        val newRequest = originalRequest.newBuilder()
            .addHeader(headerName, "$headerPrefix $apiKey")
            .build()
            
        return chain.proceed(newRequest)
    }
}
