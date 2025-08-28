package com.enlightenment.security

import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数据脱敏服务
 * 用于保护敏感信息，确保儿童隐私安全
 */
@Singleton
class DataMaskingService @Inject constructor() {
    
    /**
     * 脱敏儿童姓名
     */
    fun maskChildName(name: String): String {
        return when {
            name.isEmpty() -> ""
            name.length == 1 -> "*"
            name.length == 2 -> "${name.first()}*"
            else -> "${name.first()}${"*".repeat(name.length - 2)}${name.last()}"
        }
    }
    
    /**
     * 脱敏家长信息
     */
    fun maskParentInfo(info: String): String {
        return when {
            info.contains("@") -> maskEmail(info)
            info.matches(Regex("\\d{11}")) -> maskPhone(info)
            else -> maskGenericText(info)
        }
    }
    
    /**
     * 脱敏邮箱
     */
    fun maskEmail(email: String): String {
        val parts = email.split("@")
        if (parts.size != 2) return maskGenericText(email)
        
        val username = parts[0]
        val domain = parts[1]
        
        val maskedUsername = when {
            username.length <= 2 -> "*".repeat(username.length)
            username.length <= 4 -> "${username.take(1)}${"*".repeat(username.length - 1)}"
            else -> "${username.take(2)}${"*".repeat(3)}${username.takeLast(1)}"
        }
        
        return "$maskedUsername@$domain"
    }
    
    /**
     * 脱敏手机号
     */
    fun maskPhone(phone: String): String {
        return when {
            phone.length != 11 -> maskGenericText(phone)
            else -> "${phone.take(3)}****${phone.takeLast(4)}"
        }
    }
    
    /**
     * 脱敏通用文本
     */
    fun maskGenericText(text: String, visibleStart: Int = 2, visibleEnd: Int = 2): String {
        return when {
            text.isEmpty() -> ""
            text.length <= visibleStart + visibleEnd -> "*".repeat(text.length)
            else -> {
                val start = text.take(visibleStart)
                val end = text.takeLast(visibleEnd)
                val middle = "*".repeat(maxOf(3, text.length - visibleStart - visibleEnd))
                "$start$middle$end"
            }
        }
    }
    
    /**
     * 脱敏位置信息
     */
    fun maskLocation(location: LocationInfo): LocationInfo {
        return location.copy(
            latitude = roundLocation(location.latitude),
            longitude = roundLocation(location.longitude),
            address = maskAddress(location.address)
        )
    }
    
    /**
     * 脱敏地址
     */
    fun maskAddress(address: String): String {
        // 保留省市信息，隐藏详细地址
        val parts = address.split(" ", "，", ",")
        return when {
            parts.size <= 2 -> maskGenericText(address)
            else -> "${parts.take(2).joinToString(" ")} ***"
        }
    }
    
    /**
     * 脱敏设备信息
     */
    fun maskDeviceInfo(deviceInfo: DeviceInfo): DeviceInfo {
        return deviceInfo.copy(
            deviceId = maskGenericText(deviceInfo.deviceId, 4, 4),
            imei = maskGenericText(deviceInfo.imei, 4, 4),
            androidId = maskGenericText(deviceInfo.androidId, 4, 4),
            macAddress = maskMacAddress(deviceInfo.macAddress)
        )
    }
    
    /**
     * 脱敏MAC地址
     */
    fun maskMacAddress(mac: String): String {
        val parts = mac.split(":")
        return if (parts.size == 6) {
            "${parts[0]}:${parts[1]}:XX:XX:XX:${parts[5]}"
        } else {
            maskGenericText(mac)
        }
    }
    
    /**
     * 脱敏API密钥
     */
    fun maskApiKey(apiKey: String): String {
        return when {
            apiKey.isEmpty() -> ""
            apiKey.length <= 8 -> "*".repeat(apiKey.length)
            else -> "${apiKey.take(4)}...${"*".repeat(4)}"
        }
    }
    
    /**
     * 脱敏语音内容（儿童语音转文本结果）
     */
    fun maskVoiceContent(content: String, level: MaskingLevel = MaskingLevel.MEDIUM): String {
        return when (level) {
            MaskingLevel.LOW -> content // 不脱敏
            MaskingLevel.MEDIUM -> removeSensitiveWords(content)
            MaskingLevel.HIGH -> maskAllPersonalInfo(content)
        }
    }
    
    /**
     * 移除敏感词汇
     */
    private fun removeSensitiveWords(text: String): String {
        val sensitivePatterns = listOf(
            Regex("\\d{11}"), // 手机号
            Regex("\\d{6,}"), // 长数字串
            Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"), // 邮箱
            Regex("(?:家|住|在|地址)[^。，！？]*"), // 地址相关
        )
        
        var maskedText = text
        sensitivePatterns.forEach { pattern ->
            maskedText = maskedText.replace(pattern) { match ->
                "*".repeat(match.value.length)
            }
        }
        
        return maskedText
    }
    
    /**
     * 脱敏所有个人信息
     */
    private fun maskAllPersonalInfo(text: String): String {
        // 这是一个更严格的脱敏方法，会移除所有可能的个人信息
        val words = text.split(" ", "，", "。", "！", "？")
        return words.joinToString(" ") { word ->
            when {
                word.matches(Regex(".*[爸妈父母家姐哥弟妹].*")) -> "**"
                word.matches(Regex(".*[学校幼儿园班级].*")) -> "**"
                word.length > 4 -> maskGenericText(word, 1, 1)
                else -> word
            }
        }
    }
    
    /**
     * 四舍五入位置信息（降低精度）
     */
    private fun roundLocation(coordinate: Double): Double {
        // 保留小数点后3位，约111米精度
        return (coordinate * 1000).toInt() / 1000.0
    }
    
    /**
     * 生成数据脱敏报告
     */
    fun generateMaskingReport(
        originalData: Map<String, Any>,
        maskedData: Map<String, Any>
    ): MaskingReport {
        val maskedFields = mutableListOf<String>()
        
        originalData.forEach { (key, originalValue) ->
            val maskedValue = maskedData[key]
            if (originalValue != maskedValue) {
                maskedFields.add(key)
            }
        }
        
        return MaskingReport(
            totalFields = originalData.size,
            maskedFields = maskedFields,
            maskingLevel = determineMaskingLevel(maskedFields.size, originalData.size),
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * 确定脱敏级别
     */
    private fun determineMaskingLevel(maskedCount: Int, totalCount: Int): MaskingLevel {
        val ratio = maskedCount.toFloat() / totalCount
        return when {
            ratio < 0.3 -> MaskingLevel.LOW
            ratio < 0.7 -> MaskingLevel.MEDIUM
            else -> MaskingLevel.HIGH
        }
    }
}

/**
 * 位置信息
 */
data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val address: String
)

/**
 * 设备信息
 */
data class DeviceInfo(
    val deviceId: String,
    val imei: String,
    val androidId: String,
    val macAddress: String,
    val model: String,
    val manufacturer: String
)

/**
 * 脱敏级别
 */
enum class MaskingLevel {
    LOW,    // 低级别：仅脱敏明显的敏感信息
    MEDIUM, // 中级别：脱敏大部分个人信息
    HIGH    // 高级别：严格脱敏所有可能的个人信息
}

/**
 * 脱敏报告
 */
data class MaskingReport(
    val totalFields: Int,
    val maskedFields: List<String>,
    val maskingLevel: MaskingLevel,
    val timestamp: Long
)

/**
 * 数据脱敏扩展函数
 */
fun String.maskName() = DataMaskingService().maskChildName(this)
fun String.maskEmail() = DataMaskingService().maskEmail(this)
fun String.maskPhone() = DataMaskingService().maskPhone(this)
fun String.maskApiKey() = DataMaskingService().maskApiKey(this)