package com.enlightenment.di

import com.enlightenment.ai.model.*
import com.enlightenment.ai.model.impl.*
import com.enlightenment.ai.service.AIService
import com.enlightenment.ai.service.impl.AIServiceImpl
import com.enlightenment.BuildConfig



/**
 * AI服务依赖注入模块
 */
abstract class AIModule {
    
    @Binds
    
    abstract fun bindAIService(
        aiServiceImpl: AIServiceImpl
    ): AIService
    
    companion object {
        /**
         * 提供文本生成模型
         * 可以根据配置选择使用Gemini或GPT-5
         */
        
        
        fun provideTextGenerationModel(
            geminiModel: GeminiTextGenerationModel,
            gpt5Model: GPT5TextGenerationModel,
            mockModel: MockTextGenerationModel
        ): TextGenerationModel {
            return when {
                BuildConfig.DEBUG && !BuildConfig.USE_REAL_AI -> mockModel
                BuildConfig.PRIMARY_TEXT_MODEL == "gemini" -> geminiModel
                BuildConfig.PRIMARY_TEXT_MODEL == "gpt5" -> gpt5Model
                else -> geminiModel // 默认使用Gemini
            }
        }
        
        /**
         * 提供图像识别模型
         */
        
        
        fun provideImageRecognitionModel(
            qwenModel: QwenImageRecognitionModel,
            mockModel: MockImageRecognitionModel
        ): ImageRecognitionModel {
            return if (BuildConfig.DEBUG && !BuildConfig.USE_REAL_AI) {
                mockModel
            } else {
                qwenModel
            }
        }
        
        /**
         * 提供语音识别模型
         */
        
        
        fun provideSpeechRecognitionModel(
            openAIModel: OpenAISpeechRecognitionModel,
            mockModel: MockSpeechRecognitionModel
        ): SpeechRecognitionModel {
            return if (BuildConfig.DEBUG && !BuildConfig.USE_REAL_AI) {
                mockModel
            } else {
                openAIModel
            }
        }
        
        /**
         * 提供文本转语音模型
         */
        
        
        fun provideTextToSpeechModel(
            openAIModel: OpenAITextToSpeechModel,
            mockModel: MockTextToSpeechModel
        ): TextToSpeechModel {
            return if (BuildConfig.DEBUG && !BuildConfig.USE_REAL_AI) {
                mockModel
            } else {
                openAIModel
            }
        }
        
        /**
         * 提供Gemini模型（用于某些场景的备选）
         */
        
        
        @Named("GeminiModel")
        fun provideGeminiModel(model: GeminiTextGenerationModel): TextGenerationModel = model
        
        /**
         * 提供GPT-5模型（用于高质量内容生成）
         */
        
        
        @Named("GPT5Model")
        fun provideGPT5Model(model: GPT5TextGenerationModel): TextGenerationModel = model
    }
}
