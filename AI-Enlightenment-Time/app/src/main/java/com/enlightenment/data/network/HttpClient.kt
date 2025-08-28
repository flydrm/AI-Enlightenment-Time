package com.enlightenment.data.network

import com.enlightenment.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * HTTP客户端配置
 */
@Singleton
class HttpClient @Inject constructor() {
    
    companion object {
        private const val DEFAULT_TIMEOUT = 30L // 秒
        private const val UPLOAD_TIMEOUT = 120L // 上传大文件时的超时时间
    }
    
    /**
     * 创建通用的OkHttpClient
     */
    fun createOkHttpClient(
        interceptors: List<okhttp3.Interceptor> = emptyList(),
        timeout: Long = DEFAULT_TIMEOUT
    ): OkHttpClient {
        return OkHttpClient.Builder().apply {
            // 添加日志拦截器（仅在调试模式）
            if (com.enlightenment.BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
            }
            
            // 添加自定义拦截器
            interceptors.forEach { addInterceptor(it) }
            
            // 设置超时时间
            connectTimeout(timeout, TimeUnit.SECONDS)
            readTimeout(timeout, TimeUnit.SECONDS)
            writeTimeout(timeout, TimeUnit.SECONDS)
            
            // 连接池配置
            retryOnConnectionFailure(true)
        }.build()
    }
    
    /**
     * 创建Retrofit实例
     */
    fun createRetrofit(
        baseUrl: String,
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * 创建用于文件上传的OkHttpClient
     */
    fun createUploadOkHttpClient(
        interceptors: List<okhttp3.Interceptor> = emptyList()
    ): OkHttpClient {
        return createOkHttpClient(interceptors, UPLOAD_TIMEOUT)
    }
}