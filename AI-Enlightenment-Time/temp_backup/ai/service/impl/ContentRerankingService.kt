package com.enlightenment.ai.service.impl

import com.enlightenment.data.network.api.*
import com.enlightenment.security.SecureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 内容重排序服务
 * 使用BGE-reranker对搜索结果进行重排序，确保最相关的内容排在前面
 */
@Singleton
class ContentRerankingService @Inject constructor(
    private val bgeRerankerApi: BGERerankerApi,
    private val secureStorage: SecureStorage
) {
    
    /**
     * 对故事列表进行重排序
     */
    suspend fun rerankStories(
        query: String,
        stories: List<StoryInfo>,
        childAge: Int,
        topN: Int = 5
    ): List<StoryInfo> = withContext(Dispatchers.IO) {
        if (stories.isEmpty()) return@withContext emptyList()
        
        val apiKey = secureStorage.getBGEApiKey()
            ?: return@withContext stories // 如果没有API密钥，返回原始顺序
        
        try {
            // 创建重排序请求
            val request = ChildContentReranker.createChildFriendlyRequest(
                query = query,
                documents = stories.map { it.toDocument() },
                childAge = childAge
            )
            
            // 调用API
            val response = bgeRerankerApi.rerank("Bearer $apiKey", request)
            
            // 根据相关性分数重新排序
            val rerankedIndices = ChildContentReranker.filterChildFriendlyResults(
                response.results,
                minRelevanceScore = 0.5f
            ).map { it.index }
            
            // 返回重排序后的故事列表
            val rerankedStories = rerankedIndices.mapNotNull { index ->
                stories.getOrNull(index)
            }
            
            // 如果重排序后的列表为空，返回原始列表的前topN个
            return@withContext if (rerankedStories.isNotEmpty()) {
                rerankedStories
            } else {
                stories.take(topN)
            }
            
        } catch (e: Exception) {
            // 错误处理：返回原始顺序
            return@withContext stories.take(topN)
        }
    }
    
    /**
     * 对搜索结果进行重排序
     */
    suspend fun rerankSearchResults(
        query: String,
        results: List<SearchResult>,
        context: SearchContext
    ): List<SearchResult> = withContext(Dispatchers.IO) {
        if (results.isEmpty()) return@withContext emptyList()
        
        val apiKey = secureStorage.getBGEApiKey()
            ?: return@withContext results
        
        try {
            // 根据搜索上下文增强查询
            val enhancedQuery = buildEnhancedQuery(query, context)
            
            val request = RerankRequest(
                query = enhancedQuery,
                documents = results.map { it.content },
                topN = minOf(results.size, 10),
                returnDocuments = false
            )
            
            val response = bgeRerankerApi.rerank("Bearer $apiKey", request)
            
            // 根据重排序结果返回
            return@withContext response.results
                .sortedByDescending { it.relevanceScore }
                .mapNotNull { rankResult ->
                    results.getOrNull(rankResult.index)
                }
            
        } catch (e: Exception) {
            return@withContext results
        }
    }
    
    /**
     * 批量重排序多个查询
     */
    suspend fun batchRerank(
        queries: List<RerankingQuery>
    ): Map<String, List<Int>> = withContext(Dispatchers.IO) {
        val apiKey = secureStorage.getBGEApiKey()
            ?: return@withContext emptyMap()
        
        try {
            val batchRequest = BatchRerankRequest(
                queries = queries.map { query ->
                    RerankQuery(
                        id = query.id,
                        query = query.query,
                        documents = query.documents,
                        topN = query.topN
                    )
                }
            )
            
            val response = bgeRerankerApi.batchRerank("Bearer $apiKey", batchRequest)
            
            // 转换响应为Map
            return@withContext response.results.associate { result ->
                result.id to result.results
                    .sortedByDescending { it.relevanceScore }
                    .map { it.index }
            }
            
        } catch (e: Exception) {
            return@withContext emptyMap()
        }
    }
    
    /**
     * 构建增强查询
     */
    private fun buildEnhancedQuery(query: String, context: SearchContext): String {
        return buildString {
            append(query)
            
            if (context.childAge > 0) {
                append(" 适合${context.childAge}岁儿童")
            }
            
            if (context.educationalFocus.isNotEmpty()) {
                append(" 教育重点：${context.educationalFocus}")
            }
            
            if (context.preferSafeContent) {
                append(" 安全友好的内容")
            }
            
            if (context.language.isNotEmpty()) {
                append(" 语言：${context.language}")
            }
        }
    }
}

/**
 * 故事信息
 */
data class StoryInfo(
    val id: String,
    val title: String,
    val summary: String,
    val tags: List<String> = emptyList(),
    val ageRange: IntRange = 3..6
) {
    fun toDocument(): String {
        return buildString {
            append("标题：$title\n")
            append("简介：$summary\n")
            if (tags.isNotEmpty()) {
                append("标签：${tags.joinToString(", ")}\n")
            }
            append("适合年龄：${ageRange.first}-${ageRange.last}岁")
        }
    }
}

/**
 * 搜索结果
 */
data class SearchResult(
    val id: String,
    val title: String,
    val content: String,
    val type: ContentType,
    val score: Float = 0f
)

/**
 * 搜索上下文
 */
data class SearchContext(
    val childAge: Int = 0,
    val educationalFocus: String = "",
    val preferSafeContent: Boolean = true,
    val language: String = "中文"
)

/**
 * 重排序查询
 */
data class RerankingQuery(
    val id: String,
    val query: String,
    val documents: List<String>,
    val topN: Int = 5
)

/**
 * 内容类型
 */
enum class ContentType {
    STORY,          // 故事
    EDUCATIONAL,    // 教育内容
    GAME,          // 游戏
    ACTIVITY,      // 活动
    VIDEO,         // 视频
    AUDIO          // 音频
}