package com.enlightenment.data.local.dao

import androidx.room.*
import com.enlightenment.data.local.entity.AuditLogEntity
import com.enlightenment.security.AuditCategory
import kotlinx.coroutines.flow.Flow

/**
 * 审计日志DAO
 */
@Dao
interface AuditLogDao {
    
    /**
     * 插入审计日志
     */
    @Insert
    suspend fun insert(log: AuditLogEntity)
    
    /**
     * 批量插入审计日志
     */
    @Insert
    suspend fun insertAll(logs: List<AuditLogEntity>)
    
    /**
     * 获取所有日志（限制数量）
     */
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getAllLogs(limit: Int): Flow<List<AuditLogEntity>>
    
    /**
     * 根据类别获取日志
     */
    @Query("SELECT * FROM audit_logs WHERE category = :category ORDER BY timestamp DESC LIMIT :limit")
    fun getLogsByCategory(category: AuditCategory, limit: Int): Flow<List<AuditLogEntity>>
    
    /**
     * 获取特定时间范围的日志
     */
    @Query("""
        SELECT * FROM audit_logs 
        WHERE timestamp >= :startTime AND timestamp <= :endTime 
        ORDER BY timestamp DESC
    """)
    fun getLogsByTimeRange(startTime: Long, endTime: Long): Flow<List<AuditLogEntity>>
    
    /**
     * 获取特定用户的日志
     */
    @Query("SELECT * FROM audit_logs WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    fun getLogsByUser(userId: String, limit: Int): Flow<List<AuditLogEntity>>
    
    /**
     * 获取导出用的日志
     */
    @Query("""
        SELECT * FROM audit_logs 
        WHERE (:category IS NULL OR category = :category)
        ORDER BY timestamp DESC
    """)
    suspend fun getLogsForExport(category: AuditCategory?): List<AuditLogEntity>
    
    /**
     * 删除旧日志
     */
    @Query("DELETE FROM audit_logs WHERE timestamp < :cutoffTime")
    suspend fun deleteOldLogs(cutoffTime: Long)
    
    /**
     * 获取日志数量
     */
    @Query("SELECT COUNT(*) FROM audit_logs")
    suspend fun getLogCount(): Int
    
    /**
     * 获取特定类别的日志数量
     */
    @Query("SELECT COUNT(*) FROM audit_logs WHERE category = :category")
    suspend fun getLogCountByCategory(category: AuditCategory): Int
    
    /**
     * 搜索日志
     */
    @Query("""
        SELECT * FROM audit_logs 
        WHERE action LIKE '%' || :query || '%' 
        OR details LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    fun searchLogs(query: String, limit: Int): Flow<List<AuditLogEntity>>
    
    /**
     * 获取最近的错误日志
     */
    @Query("""
        SELECT * FROM audit_logs 
        WHERE category = 'ERROR' 
        ORDER BY timestamp DESC 
        LIMIT :limit
    """)
    fun getRecentErrors(limit: Int): Flow<List<AuditLogEntity>>
    
    /**
     * 清空所有日志（仅用于测试或重置）
     */
    @Query("DELETE FROM audit_logs")
    suspend fun deleteAllLogs()
}