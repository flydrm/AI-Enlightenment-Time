package com.enlightenment.di

import com.enlightenment.ai.model.*
import com.enlightenment.ai.model.impl.*
import com.enlightenment.ai.service.AIService
import com.enlightenment.ai.service.impl.AIServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * AI服务依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AIModule {
    
    @Binds
    @Singleton
    abstract fun bindAIService(
        aiServiceImpl: AIServiceImpl
    ): AIService
    
    @Binds
    @Singleton
    abstract fun bindTextGenerationModel(
        mockTextGenerationModel: MockTextGenerationModel
    ): TextGenerationModel
    
    @Binds
    @Singleton
    abstract fun bindImageRecognitionModel(
        mockImageRecognitionModel: MockImageRecognitionModel
    ): ImageRecognitionModel
    
    @Binds
    @Singleton
    abstract fun bindSpeechRecognitionModel(
        mockSpeechRecognitionModel: MockSpeechRecognitionModel
    ): SpeechRecognitionModel
    
    @Binds
    @Singleton
    abstract fun bindTextToSpeechModel(
        mockTextToSpeechModel: MockTextToSpeechModel
    ): TextToSpeechModel
}