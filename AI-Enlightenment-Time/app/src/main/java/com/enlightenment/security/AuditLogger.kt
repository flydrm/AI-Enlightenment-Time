package com.enlightenment.security

import android.content.Context
import com.enlightenment.data.local.database.AppDatabase
import com.enlightenment.data.local.entity.AuditLogEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 审计日志记录器
 * 记录所有重要操作，用于安全审计和问题追踪
 */
@Singleton
class AuditLogger @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val secureStorage: SecureStorage
) {
    
    private val auditScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * 记录用户操作
     */
    fun logUserAction(
        action: UserAction,
        details: String? = null,
        metadata: Map<String, String> = emptyMap()
    ) {
        auditScope.launch {
            val log = AuditLogEntity(
                timestamp = System.currentTimeMillis(),
                category = AuditCategory.USER_ACTION,
                action = action.name,
                details = details,
                metadata = metadata,
                userId = getCurrentUserId()
            )
            database.auditLogDao().insert(log)
        }
    }
    
    /**
     * 记录AI API调用
     */
    fun logApiCall(
        service: String,
        endpoint: String,
        success: Boolean,
        errorMessage: String? = null,
        responseTime: Long? = null
    ) {
        auditScope.launch {
            val log = AuditLogEntity(
                timestamp = System.currentTimeMillis(),
                category = AuditCategory.API_CALL,
                action = "API_CALL_$service",
                details = buildString {
                    append("Service: $service\n")
                    append("Endpoint: $endpoint\n")
                    append("Success: $success\n")
                    errorMessage?.let { append("Error: $it\n") }
                    responseTime?.let { append("Response Time: ${it}ms\n") }
                },
                metadata = mapOf(
                    "service" to service,
                    "endpoint" to endpoint,
                    "success" to success.toString(),
                    "response_time" to (responseTime?.toString() ?: "")
                ),
                userId = getCurrentUserId()
            )
            database.auditLogDao().insert(log)
        }
    }
    
    /**
     * 记录安全事件
     */
    fun logSecurityEvent(
        event: SecurityEvent,
        details: String,
        severity: SecuritySeverity = SecuritySeverity.INFO
    ) {
        auditScope.launch {
            val log = AuditLogEntity(
                timestamp = System.currentTimeMillis(),
                category = AuditCategory.SECURITY,
                action = event.name,
                details = details,
                metadata = mapOf(
                    "severity" to severity.name
                ),
                userId = getCurrentUserId()
            )
            database.auditLogDao().insert(log)
        }
    }
    
    /**
     * 记录数据访问
     */
    fun logDataAccess(
        dataType: DataType,
        operation: DataOperation,
        recordId: String? = null,
        details: String? = null
    ) {
        auditScope.launch {
            val log = AuditLogEntity(
                timestamp = System.currentTimeMillis(),
                category = AuditCategory.DATA_ACCESS,
                action = "${dataType.name}_${operation.name}",
                details = details,
                metadata = mapOf(
                    "data_type" to dataType.name,
                    "operation" to operation.name,
                    "record_id" to (recordId ?: "")
                ),
                userId = getCurrentUserId()
            )
            database.auditLogDao().insert(log)
        }
    }
    
    /**
     * 记录错误
     */
    fun logError(
        errorType: String,
        message: String,
        stackTrace: String? = null,
        context: Map<String, String> = emptyMap()
    ) {
        auditScope.launch {
            val log = AuditLogEntity(
                timestamp = System.currentTimeMillis(),
                category = AuditCategory.ERROR,
                action = "ERROR_$errorType",
                details = buildString {
                    append("Message: $message\n")
                    stackTrace?.let { append("Stack Trace:\n$it\n") }
                    if (context.isNotEmpty()) {
                        append("Context:\n")
                        context.forEach { (key, value) ->
                            append("  $key: $value\n")
                        }
                    }
                },
                metadata = context + mapOf("error_type" to errorType),
                userId = getCurrentUserId()
            )
            database.auditLogDao().insert(log)
        }
    }
    
    /**
     * 获取审计日志
     */
    fun getAuditLogs(
        category: AuditCategory? = null,
        startTime: Long? = null,
        endTime: Long? = null,
        limit: Int = 100
    ): Flow<List<AuditLogEntity>> {
        return if (category != null) {
            database.auditLogDao().getLogsByCategory(category, limit)
        } else {
            database.auditLogDao().getAllLogs(limit)
        }
    }
    
    /**
     * 清理旧日志
     */
    suspend fun cleanupOldLogs(daysToKeep: Int = 30) {
        val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        database.auditLogDao().deleteOldLogs(cutoffTime)
    }
    
    /**
     * 导出日志（用于家长查看）
     */
    suspend fun exportLogs(
        category: AuditCategory? = null,
        format: ExportFormat = ExportFormat.JSON
    ): String {
        val logs = database.auditLogDao().getLogsForExport(category)
        
        return when (format) {
            ExportFormat.JSON -> exportAsJson(logs)
            ExportFormat.CSV -> exportAsCsv(logs)
        }
    }
    
    /**
     * 获取当前用户ID
     */
    private fun getCurrentUserId(): String {
        return secureStorage.getChildProfile()?.name ?: "unknown"
    }
    
    /**
     * 导出为JSON
     */
    private fun exportAsJson(logs: List<AuditLogEntity>): String {
        // 简单的JSON导出实现
        return logs.joinToString(",", "[", "]") { log ->
            """
            {
                "timestamp": ${log.timestamp},
                "date": "${Date(log.timestamp)}",
                "category": "${log.category}",
                "action": "${log.action}",
                "details": "${log.details?.replace("\"", "\\\"")}",
                "user": "${log.userId}"
            }
            """.trimIndent()
        }
    }
    
    /**
     * 导出为CSV
     */
    private fun exportAsCsv(logs: List<AuditLogEntity>): String {
        val header = "Timestamp,Date,Category,Action,Details,User\n"
        val rows = logs.joinToString("\n") { log ->
            "${log.timestamp},\"${Date(log.timestamp)}\",${log.category},${log.action},\"${log.details ?: ""}\",${log.userId}"
        }
        return header + rows
    }
}

/**
 * 审计类别
 */
enum class AuditCategory {
    USER_ACTION,    // 用户操作
    API_CALL,       // API调用
    SECURITY,       // 安全事件
    DATA_ACCESS,    // 数据访问
    ERROR,          // 错误
    SYSTEM          // 系统事件
}

/**
 * 用户操作
 */
enum class UserAction {
    // 应用操作
    APP_LAUNCH,
    APP_CLOSE,
    
    // 故事相关
    STORY_START,
    STORY_COMPLETE,
    STORY_SKIP,
    STORY_FAVORITE,
    
    // 语音相关
    VOICE_RECORDING_START,
    VOICE_RECORDING_STOP,
    VOICE_PLAYBACK,
    
    // 相机相关
    CAMERA_CAPTURE,
    CAMERA_PERMISSION_GRANTED,
    CAMERA_PERMISSION_DENIED,
    
    // 设置相关
    SETTINGS_ACCESS,
    SETTINGS_CHANGE,
    PARENT_PIN_ATTEMPT,
    PARENT_PIN_SUCCESS,
    PARENT_PIN_FAILURE,
    
    // 成就相关
    ACHIEVEMENT_UNLOCKED,
    ACHIEVEMENT_VIEWED
}

/**
 * 安全事件
 */
enum class SecurityEvent {
    PIN_VERIFICATION_SUCCESS,
    PIN_VERIFICATION_FAILURE,
    API_KEY_ACCESS,
    SUSPICIOUS_ACTIVITY,
    DATA_ENCRYPTION_ERROR,
    PERMISSION_CHANGE
}

/**
 * 安全严重程度
 */
enum class SecuritySeverity {
    INFO,
    WARNING,
    ERROR,
    CRITICAL
}

/**
 * 数据类型
 */
enum class DataType {
    USER_PROFILE,
    STORY,
    ACHIEVEMENT,
    PROGRESS,
    SETTINGS,
    API_KEY
}

/**
 * 数据操作
 */
enum class DataOperation {
    CREATE,
    READ,
    UPDATE,
    DELETE
}

/**
 * 导出格式
 */
enum class ExportFormat {
    JSON,
    CSV
}