package com.enlightenment.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 安全管理器
 * 负责数据加密、解密、安全存储等功能
 */
@Singleton
class SecurityManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val ENCRYPTION_KEY_ALIAS = "EnlightenmentEncryptionKey"
        private const val ENCRYPTION_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val ENCRYPTED_PREFS_FILE = "enlightenment_encrypted_prefs"
        private const val GCM_TAG_LENGTH = 128
    }
    
    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
    }
    
    private val encryptedSharedPreferences by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        
        EncryptedSharedPreferences.create(
            ENCRYPTED_PREFS_FILE,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    init {
        generateEncryptionKey()
    }
    
    /**
     * 生成加密密钥
     */
    private fun generateEncryptionKey() {
        if (!keyStore.containsAlias(ENCRYPTION_KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                ENCRYPTION_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false)
                .setRandomizedEncryptionRequired(true)
                .build()
            
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }
    
    /**
     * 获取加密密钥
     */
    private fun getSecretKey(): SecretKey {
        return keyStore.getKey(ENCRYPTION_KEY_ALIAS, null) as SecretKey
    }
    
    /**
     * 加密数据
     */
    fun encrypt(plainText: String): EncryptedData {
        val cipher = Cipher.getInstance(ENCRYPTION_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
        
        return EncryptedData(
            encryptedData = Base64.encodeToString(encryptedBytes, Base64.DEFAULT),
            iv = Base64.encodeToString(iv, Base64.DEFAULT)
        )
    }
    
    /**
     * 解密数据
     */
    fun decrypt(encryptedData: EncryptedData): String {
        val cipher = Cipher.getInstance(ENCRYPTION_TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, Base64.decode(encryptedData.iv, Base64.DEFAULT))
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        
        val decryptedBytes = cipher.doFinal(
            Base64.decode(encryptedData.encryptedData, Base64.DEFAULT)
        )
        
        return String(decryptedBytes, StandardCharsets.UTF_8)
    }
    
    /**
     * 安全存储数据
     */
    fun saveSecureData(key: String, value: String) {
        encryptedSharedPreferences.edit()
            .putString(key, value)
            .apply()
    }
    
    /**
     * 获取安全存储的数据
     */
    fun getSecureData(key: String): String? {
        return encryptedSharedPreferences.getString(key, null)
    }
    
    /**
     * 删除安全存储的数据
     */
    fun removeSecureData(key: String) {
        encryptedSharedPreferences.edit()
            .remove(key)
            .apply()
    }
    
    /**
     * 清除所有安全存储的数据
     */
    fun clearAllSecureData() {
        encryptedSharedPreferences.edit()
            .clear()
            .apply()
    }
    
    /**
     * 保存API密钥
     */
    fun saveApiKey(service: String, apiKey: String) {
        saveSecureData("api_key_$service", apiKey)
    }
    
    /**
     * 获取API密钥
     */
    fun getApiKey(service: String): String? {
        return getSecureData("api_key_$service")
    }
    
    /**
     * 验证数据完整性
     */
    fun generateHash(data: String): String {
        val messageDigest = java.security.MessageDigest.getInstance("SHA-256")
        val hashBytes = messageDigest.digest(data.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(hashBytes, Base64.DEFAULT)
    }
    
    /**
     * 验证哈希值
     */
    fun verifyHash(data: String, hash: String): Boolean {
        val calculatedHash = generateHash(data)
        return calculatedHash == hash
    }
    
    /**
     * 安全删除密钥（如需要）
     */
    fun deleteEncryptionKey() {
        if (keyStore.containsAlias(ENCRYPTION_KEY_ALIAS)) {
            keyStore.deleteEntry(ENCRYPTION_KEY_ALIAS)
        }
    }
    
    /**
     * 加密数据类
     */
    data class EncryptedData(
        val encryptedData: String,
        val iv: String
    )
}