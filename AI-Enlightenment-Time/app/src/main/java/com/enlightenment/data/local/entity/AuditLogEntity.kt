package com.enlightenment.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.enlightenment.data.local.converter.Converters
import com.enlightenment.security.AuditCategory



/**
 * 审计日志实体
 */
@Entity(tableName = "audit_logs")
@TypeConverters(Converters::class)
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /**
     * 时间戳
     */
    val timestamp: Long,
    
    /**
     * 审计类别
     */
    val category: AuditCategory,
    
    /**
     * 操作名称
     */
    val action: String,
    
    /**
     * 详细信息
     */
    val details: String? = null,
    
    /**
     * 元数据（JSON格式存储）
     */
    val metadata: Map<String, String> = emptyMap(),
    
    /**
     * 用户ID
     */
    val userId: String,
    
    /**
     * 设备信息
     */
    val deviceInfo: String? = null,
    
    /**
     * 应用版本
     */
    val appVersion: String? = null
)
