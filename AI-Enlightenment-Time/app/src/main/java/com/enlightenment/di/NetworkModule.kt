package com.enlightenment.di

import com.enlightenment.data.network.api.*
import com.enlightenment.data.network.HttpClient
import com.enlightenment.data.network.interceptor.ApiKeyInterceptor
import com.enlightenment.data.network.interceptor.RetryInterceptor
import com.enlightenment.security.SecureStorage



/**
 * 网络依赖注入模块
 */
object NetworkModule {
    
    
    
    fun provideHttpClient(): HttpClient = HttpClient()
    
    /**
     * Gemini API
     */
    
    
    @Named("GeminiOkHttp")
    fun provideGeminiOkHttpClient(
        httpClient: HttpClient,
        secureStorage: SecureStorage
    ): okhttp3.OkHttpClient {
        return httpClient.createOkHttpClient(
            interceptors = listOf(
                RetryInterceptor(),
                // Gemini使用查询参数传递API密钥，不需要ApiKeyInterceptor
            )
        )
    }
    
    
    
    fun provideGeminiApi(
        httpClient: HttpClient,
        @Named("GeminiOkHttp") okHttpClient: okhttp3.OkHttpClient
    ): GeminiApi {
        val retrofit = httpClient.createRetrofit(
            baseUrl = GeminiApi.BASE_URL,
            okHttpClient = okHttpClient
        )
        return retrofit.create(GeminiApi::class.java)
    }
    
    /**
     * OpenAI API (GPT-5)
     */
    
    
    @Named("OpenAIOkHttp")
    fun provideOpenAIOkHttpClient(
        httpClient: HttpClient,
        secureStorage: SecureStorage
    ): okhttp3.OkHttpClient {
        return httpClient.createOkHttpClient(
            interceptors = listOf(
                ApiKeyInterceptor(
                    apiKeyProvider = { secureStorage.getOpenAIApiKey() ?: "" }
                ),
                RetryInterceptor()
            )
        )
    }
    
    
    
    fun provideOpenAIApi(
        httpClient: HttpClient,
        @Named("OpenAIOkHttp") okHttpClient: okhttp3.OkHttpClient
    ): OpenAIApi {
        val retrofit = httpClient.createRetrofit(
            baseUrl = OpenAIApi.BASE_URL,
            okHttpClient = okHttpClient
        )
        return retrofit.create(OpenAIApi::class.java)
    }
    
    /**
     * Grok Image API
     */
    
    
    @Named("GrokOkHttp")
    fun provideGrokOkHttpClient(
        httpClient: HttpClient,
        secureStorage: SecureStorage
    ): okhttp3.OkHttpClient {
        return httpClient.createOkHttpClient(
            interceptors = listOf(
                ApiKeyInterceptor(
                    apiKeyProvider = { secureStorage.getGrokApiKey() ?: "" }
                ),
                RetryInterceptor()
            )
        )
    }
    
    
    
    fun provideGrokImageApi(
        httpClient: HttpClient,
        @Named("GrokOkHttp") okHttpClient: okhttp3.OkHttpClient
    ): GrokImageApi {
        val retrofit = httpClient.createRetrofit(
            baseUrl = GrokImageApi.BASE_URL,
            okHttpClient = okHttpClient
        )
        return retrofit.create(GrokImageApi::class.java)
    }
    
    /**
     * Qwen API
     */
    
    
    @Named("QwenOkHttp")
    fun provideQwenOkHttpClient(
        httpClient: HttpClient,
        secureStorage: SecureStorage
    ): okhttp3.OkHttpClient {
        return httpClient.createOkHttpClient(
            interceptors = listOf(
                ApiKeyInterceptor(
                    apiKeyProvider = { secureStorage.getQwenApiKey() ?: "" }
                ),
                RetryInterceptor()
            )
        )
    }
    
    
    
    fun provideQwenApi(
        httpClient: HttpClient,
        @Named("QwenOkHttp") okHttpClient: okhttp3.OkHttpClient
    ): QwenApi {
        val retrofit = httpClient.createRetrofit(
            baseUrl = QwenApi.BASE_URL,
            okHttpClient = okHttpClient
        )
        return retrofit.create(QwenApi::class.java)
    }
    
    /**
     * BGE Reranker API
     */
    
    
    @Named("BGEOkHttp")
    fun provideBGEOkHttpClient(
        httpClient: HttpClient,
        secureStorage: SecureStorage
    ): okhttp3.OkHttpClient {
        return httpClient.createOkHttpClient(
            interceptors = listOf(
                ApiKeyInterceptor(
                    apiKeyProvider = { secureStorage.getBGEApiKey() ?: "" }
                ),
                RetryInterceptor()
            )
        )
    }
    
    
    
    fun provideBGERerankerApi(
        httpClient: HttpClient,
        @Named("BGEOkHttp") okHttpClient: okhttp3.OkHttpClient
    ): BGERerankerApi {
        val retrofit = httpClient.createRetrofit(
            baseUrl = BGERerankerApi.BASE_URL,
            okHttpClient = okHttpClient
        )
        return retrofit.create(BGERerankerApi::class.java)
    }
}
