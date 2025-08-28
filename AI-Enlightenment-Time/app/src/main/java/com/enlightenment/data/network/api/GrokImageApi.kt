package com.enlightenment.data.network.api

import retrofit2.http.*



/**
 * Grok-4 图像生成API接口
 */
interface GrokImageApi {
    
    companion object {
        const val BASE_URL = "https://api.x.ai/"
        const val API_VERSION = "v1"
        const val MODEL_NAME = "grok-4-imageGen"
    }
    
    /**
     * 生成图像
     */
    @POST("$API_VERSION/images/generations")
    suspend fun generateImage(
        @Header("Authorization") authorization: String,
        @Body request: ImageGenerationRequest
    ): ImageGenerationResponse
    
    /**
     * 编辑图像
     */
    @Multipart
    @POST("$API_VERSION/images/edits")
    suspend fun editImage(
        @Header("Authorization") authorization: String,
        @Part image: okhttp3.MultipartBody.Part,
        @Part mask: okhttp3.MultipartBody.Part?,
        @Part("prompt") prompt: String,
        @Part("n") n: Int? = null,
        @Part("size") size: String? = null
    ): ImageGenerationResponse
    
    /**
     * 创建图像变体
     */
    @Multipart
    @POST("$API_VERSION/images/variations")
    suspend fun createImageVariation(
        @Header("Authorization") authorization: String,
        @Part image: okhttp3.MultipartBody.Part,
        @Part("n") n: Int? = null,
        @Part("size") size: String? = null
    ): ImageGenerationResponse
}
/**
 * 图像生成请求
 */
data class ImageGenerationRequest(
    val model: String = GrokImageApi.MODEL_NAME,
    val prompt: String,
    val n: Int? = 1,
    val size: String? = "1024x1024", // 256x256, 512x512, 1024x1024, 1792x1024, 1024x1792
    val quality: String? = "standard", // standard, hd
    val style: String? = null, // vivid, natural
    val responseFormat: String? = "url", // url, b64_json
    val user: String? = null
) {
    companion object {
        // 儿童友好的图像生成配置
        fun childFriendly(prompt: String) = ImageGenerationRequest(
            prompt = buildString {
                append("Create a child-friendly, colorful, and whimsical illustration. ")
                append("Style: children's book illustration, bright colors, safe content. ")
                append("Avoid: scary elements, violence, inappropriate content. ")
                append("Description: ")
                append(prompt)
            },
            quality = "hd",
            style = "vivid"
        )
    }
}
/**
 * 图像生成响应
 */
data class ImageGenerationResponse(
    val created: Long,
    val data: List<ImageData>
)
/**
 * 图像数据
 */
data class ImageData(
    val url: String? = null,
    val b64Json: String? = null,
    val revisedPrompt: String? = null
)
