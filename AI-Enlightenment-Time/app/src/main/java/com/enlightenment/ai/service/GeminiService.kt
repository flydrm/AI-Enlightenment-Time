package com.enlightenment.ai.service

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import com.enlightenment.data.network.api.Content
import com.enlightenment.data.network.api.GeminiApi
import com.enlightenment.data.network.api.GeminiRequest
import com.enlightenment.data.network.api.Part
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext



/**
 * Service for interacting with Gemini AI model
 */
class GeminiService(
    private val geminiApi: GeminiApi
) {
    
    /**
     * Generate content using Gemini AI
     * @param prompt The prompt to generate content from
     * @return Result containing the generated content or error
     */
    suspend fun generateContent(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        role = "user",
                        parts = listOf(Part.TextPart(prompt))
                    )
                )
            )
            
            val response = geminiApi.generateContent(request)
            
            val generatedText = response.candidates.firstOrNull()
                ?.content
                ?.parts
                ?.filterIsInstance<Part.TextPart>()
                ?.firstOrNull()
                ?.text
                ?: return@withContext Result.failure(Exception("No content generated"))
            
            Result.success(generatedText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
