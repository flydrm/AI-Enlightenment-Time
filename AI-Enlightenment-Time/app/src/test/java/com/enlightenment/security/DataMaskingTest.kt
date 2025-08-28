package com.enlightenment.security

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DataMaskingTest {
    
    private lateinit var dataMaskingService: DataMaskingService
    
    @Before
    fun setup() {
        dataMaskingService = DataMaskingService()
    }
    
    @Test
    fun `test maskName masks Chinese names correctly`() {
        // Given
        val testCases = mapOf(
            "张三" to "张*",
            "李四五" to "李**",
            "欧阳大明" to "欧阳**",
            "司马相如" to "司马**"
        )
        
        // When & Then
        testCases.forEach { (input, expected) ->
            val result = dataMaskingService.maskName(input)
            assertEquals(expected, result, "Failed to mask name: $input")
        }
    }
    
    @Test
    fun `test maskEmail masks email correctly`() {
        // Given
        val testCases = mapOf(
            "test@example.com" to "t***@example.com",
            "user123@gmail.com" to "u******@gmail.com",
            "a@b.c" to "a@b.c", // 太短不脱敏
            "longusername@domain.com" to "l***********@domain.com"
        )
        
        // When & Then
        testCases.forEach { (input, expected) ->
            val result = dataMaskingService.maskEmail(input)
            assertEquals(expected, result, "Failed to mask email: $input")
        }
    }
    
    @Test
    fun `test maskPhone masks phone number correctly`() {
        // Given
        val testCases = mapOf(
            "13812345678" to "138****5678",
            "18687654321" to "186****4321",
            "12345" to "12345", // 太短不脱敏
            "+8613812345678" to "+86138****5678"
        )
        
        // When & Then
        testCases.forEach { (input, expected) ->
            val result = dataMaskingService.maskPhone(input)
            assertEquals(expected, result, "Failed to mask phone: $input")
        }
    }
    
    @Test
    fun `test maskIdCard masks ID card correctly`() {
        // Given
        val idCard = "110101199001011234"
        val expected = "110101********1234"
        
        // When
        val result = dataMaskingService.maskIdCard(idCard)
        
        // Then
        assertEquals(expected, result)
    }
    
    @Test
    fun `test maskAddress masks address correctly`() {
        // Given
        val testCases = mapOf(
            "北京市朝阳区某某街道123号" to "北京市朝阳区******",
            "上海市浦东新区张江高科技园区" to "上海市浦东新区******",
            "广东省深圳市南山区科技园南路" to "广东省深圳市南山区******"
        )
        
        // When & Then
        testCases.forEach { (input, expected) ->
            val result = dataMaskingService.maskAddress(input)
            assertEquals(expected, result, "Failed to mask address: $input")
        }
    }
    
    @Test
    fun `test maskSensitiveText masks multiple sensitive data types`() {
        // Given
        val text = """
            用户信息：
            姓名：张三
            电话：13812345678
            邮箱：zhangsan@example.com
            地址：北京市朝阳区某某街道123号
        """.trimIndent()
        
        // When
        val result = dataMaskingService.maskSensitiveText(text)
        
        // Then
        assertNotEquals(text, result)
        assert(result.contains("张*"))
        assert(result.contains("138****5678"))
        assert(result.contains("z*******@example.com"))
        assert(result.contains("北京市朝阳区******"))
    }
    
    @Test
    fun `test maskChildName preserves child safety`() {
        // Given
        val childNames = listOf("小明", "宝宝", "小朋友")
        
        // When & Then
        childNames.forEach { name ->
            val result = dataMaskingService.maskChildName(name)
            assertEquals("小朋友", result)
        }
    }
}