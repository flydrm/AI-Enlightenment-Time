package com.enlightenment.data.network.interceptor

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * 重试拦截器
 */
class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val retryDelayMillis: Long = 1000L,
    private val backoffMultiplier: Float = 2f
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        var lastException: IOException? = null
        var currentDelay = retryDelayMillis
        
        for (attempt in 0..maxRetries) {
            try {
                val response = chain.proceed(originalRequest)
                
                // 如果响应成功或不需要重试的错误码，直接返回
                if (response.isSuccessful || !shouldRetry(response.code)) {
                    return response
                }
                
                // 关闭失败的响应
                response.close()
                
                // 如果是最后一次尝试，返回失败响应
                if (attempt == maxRetries) {
                    return response
                }
                
            } catch (e: IOException) {
                lastException = e
                
                // 如果是最后一次尝试，抛出异常
                if (attempt == maxRetries) {
                    throw e
                }
            }
            
            // 等待后重试
            runBlocking {
                delay(currentDelay)
            }
            
            // 增加下次重试的延迟时间（指数退避）
            currentDelay = (currentDelay * backoffMultiplier).toLong()
        }
        
        throw lastException ?: IOException("Max retries reached")
    }
    
    /**
     * 判断是否应该重试
     */
    private fun shouldRetry(code: Int): Boolean {
        return when (code) {
            408, // Request Timeout
            429, // Too Many Requests
            500, // Internal Server Error
            502, // Bad Gateway
            503, // Service Unavailable
            504  // Gateway Timeout
            -> true
            else -> false
        }
    }
}