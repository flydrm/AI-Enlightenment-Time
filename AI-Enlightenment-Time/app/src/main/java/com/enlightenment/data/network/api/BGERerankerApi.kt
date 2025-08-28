package com.enlightenment.data.network.api

import retrofit2.http.*



/**
 * BAAI/bge-reranker-v2-m3 重排序API接口
 * 用于搜索结果重排序
 */
interface BGERerankerApi {
    
    companion object {
        const val BASE_URL = "https://api.siliconflow.cn/"
        const val API_VERSION = "v1"
        const val MODEL_NAME = "BAAI/bge-reranker-v2-m3"
    }
    
    /**
     * 重排序
     */
    @POST("$API_VERSION/rerank")
    suspend fun rerank(
        @Header("Authorization") authorization: String,
        @Body request: RerankRequest
    ): RerankResponse
    
    /**
     * 批量重排序
     */
    @POST("$API_VERSION/batch/rerank")
    suspend fun batchRerank(
        @Header("Authorization") authorization: String,
        @Body request: BatchRerankRequest
    ): BatchRerankResponse
}
/**
 * 重排序请求
 */
data class RerankRequest(
    val model: String = BGERerankerApi.MODEL_NAME,
    val query: String,
    val documents: List<String>,
    val topN: Int? = null,
    val returnDocuments: Boolean? = true
)
/**
 * 重排序响应
 */
data class RerankResponse(
    val results: List<RerankResult>,
    val meta: RerankMeta? = null
)
/**
 * 重排序结果
 */
data class RerankResult(
    val index: Int,
    val relevanceScore: Float,
    val document: String? = null
)
/**
 * 重排序元数据
 */
data class RerankMeta(
    val apiVersion: String,
    val billedUnits: Int? = null
)
/**
 * 批量重排序请求
 */
data class BatchRerankRequest(
    val model: String = BGERerankerApi.MODEL_NAME,
    val queries: List<RerankQuery>,
    val returnDocuments: Boolean? = true
)
/**
 * 重排序查询
 */
data class RerankQuery(
    val id: String,
    val query: String,
    val documents: List<String>,
    val topN: Int? = null
)
/**
 * 批量重排序响应
 */
data class BatchRerankResponse(
    val results: List<BatchRerankResult>,
    val meta: RerankMeta? = null
)
/**
 * 批量重排序结果
 */
data class BatchRerankResult(
    val id: String,
    val results: List<RerankResult>
)
/**
 * 儿童内容重排序助手
 */
object ChildContentReranker {
    
    /**
     * 创建儿童友好的重排序请求
     */
    fun createChildFriendlyRequest(
        query: String,
        documents: List<String>,
        childAge: Int
    ): RerankRequest {
        // 增强查询，考虑儿童年龄和理解能力
        val enhancedQuery = buildString {
            append("适合${childAge}岁儿童的内容：")
            append(query)
            append("。要求：简单易懂、安全健康、有教育意义。")
        }
        
        return RerankRequest(
            query = enhancedQuery,
            documents = documents,
            topN = minOf(5, documents.size), // 返回前5个最相关的结果
            returnDocuments = true
        )
    }
    
    /**
     * 过滤和排序儿童内容
     */
    fun filterChildFriendlyResults(
        results: List<RerankResult>,
        minRelevanceScore: Float = 0.7f
    ): List<RerankResult> {
        return results
            .filter { it.relevanceScore >= minRelevanceScore }
            .sortedByDescending { it.relevanceScore }
    }
}
