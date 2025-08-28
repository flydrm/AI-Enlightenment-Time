package com.enlightenment.presentation.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.security.AuditLogger
import com.enlightenment.security.SecureStorage
import com.enlightenment.security.SecurityEvent
import com.enlightenment.security.SecuritySeverity
import com.enlightenment.security.UserAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

/**
 * 家长认证视图模型
 */
@HiltViewModel
class ParentAuthViewModel @Inject constructor(
    private val secureStorage: SecureStorage,
    private val auditLogger: AuditLogger
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ParentAuthUiState())
    val uiState: StateFlow<ParentAuthUiState> = _uiState.asStateFlow()
    
    companion object {
        private const val MAX_ATTEMPTS = 5
        private const val LOCKOUT_DURATION_MS = 300000L // 5分钟
    }
    
    init {
        checkLockoutStatus()
        generateMathChallenge()
    }
    
    /**
     * PIN码输入变化
     */
    fun onPinChange(pin: String) {
        if (pin.length <= 6 && pin.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(
                pinCode = pin,
                errorMessage = null
            )
        }
    }
    
    /**
     * 数学答案输入变化
     */
    fun onMathAnswerChange(answer: String) {
        if (answer.all { it.isDigit() || it == '-' }) {
            _uiState.value = _uiState.value.copy(
                mathAnswer = answer,
                errorMessage = null
            )
        }
    }
    
    /**
     * 切换验证方式
     */
    fun toggleAuthMethod() {
        _uiState.value = _uiState.value.copy(
            authMethod = when (_uiState.value.authMethod) {
                AuthMethod.PIN -> AuthMethod.MATH_CHALLENGE
                AuthMethod.MATH_CHALLENGE -> AuthMethod.PIN
            },
            pinCode = "",
            mathAnswer = "",
            errorMessage = null
        )
        
        if (_uiState.value.authMethod == AuthMethod.MATH_CHALLENGE) {
            generateMathChallenge()
        }
    }
    
    /**
     * 执行认证
     */
    suspend fun authenticate(): Boolean {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        // 模拟网络延迟
        delay(500)
        
        val success = when (_uiState.value.authMethod) {
            AuthMethod.PIN -> verifyPin()
            AuthMethod.MATH_CHALLENGE -> verifyMathAnswer()
        }
        
        if (success) {
            auditLogger.logUserAction(
                UserAction.PARENT_PIN_SUCCESS,
                "家长验证成功",
                mapOf("method" to _uiState.value.authMethod.name)
            )
            auditLogger.logSecurityEvent(
                SecurityEvent.PIN_VERIFICATION_SUCCESS,
                "家长通过${_uiState.value.authMethod.name}验证"
            )
        } else {
            val remainingAttempts = MAX_ATTEMPTS - _uiState.value.failedAttempts - 1
            
            auditLogger.logUserAction(
                UserAction.PARENT_PIN_FAILURE,
                "家长验证失败",
                mapOf(
                    "method" to _uiState.value.authMethod.name,
                    "remaining_attempts" to remainingAttempts.toString()
                )
            )
            
            if (remainingAttempts <= 0) {
                lockout()
            } else {
                _uiState.value = _uiState.value.copy(
                    failedAttempts = _uiState.value.failedAttempts + 1,
                    attemptsRemaining = remainingAttempts,
                    errorMessage = "验证失败，请重试（剩余${remainingAttempts}次）"
                )
            }
        }
        
        _uiState.value = _uiState.value.copy(isLoading = false)
        return success
    }
    
    /**
     * 验证PIN码
     */
    private fun verifyPin(): Boolean {
        return secureStorage.verifyParentPin(_uiState.value.pinCode)
    }
    
    /**
     * 验证数学答案
     */
    private fun verifyMathAnswer(): Boolean {
        val userAnswer = _uiState.value.mathAnswer.toIntOrNull() ?: return false
        return userAnswer == _uiState.value.correctMathAnswer
    }
    
    /**
     * 生成数学题
     */
    private fun generateMathChallenge() {
        val a = Random.nextInt(10, 50)
        val b = Random.nextInt(10, 50)
        val operators = listOf("+", "-", "×")
        val operator = operators.random()
        
        val (challenge, answer) = when (operator) {
            "+" -> "$a + $b = ?" to (a + b)
            "-" -> "$a - $b = ?" to (a - b)
            "×" -> "$a × $b = ?" to (a * b)
            else -> "$a + $b = ?" to (a + b)
        }
        
        _uiState.value = _uiState.value.copy(
            mathChallenge = challenge,
            correctMathAnswer = answer
        )
    }
    
    /**
     * 检查锁定状态
     */
    private fun checkLockoutStatus() {
        val lockoutEndTime = secureStorage.getParentAuthLockoutTime()
        if (lockoutEndTime > System.currentTimeMillis()) {
            _uiState.value = _uiState.value.copy(
                isLockedOut = true,
                lockoutEndTime = lockoutEndTime,
                errorMessage = "验证已锁定，请稍后再试"
            )
            
            // 启动倒计时
            viewModelScope.launch {
                while (_uiState.value.lockoutEndTime > System.currentTimeMillis()) {
                    delay(1000)
                    if (_uiState.value.lockoutEndTime <= System.currentTimeMillis()) {
                        _uiState.value = _uiState.value.copy(
                            isLockedOut = false,
                            lockoutEndTime = 0,
                            errorMessage = null,
                            failedAttempts = 0,
                            attemptsRemaining = MAX_ATTEMPTS
                        )
                    }
                }
            }
        }
    }
    
    /**
     * 锁定认证
     */
    private fun lockout() {
        val lockoutEndTime = System.currentTimeMillis() + LOCKOUT_DURATION_MS
        secureStorage.setParentAuthLockoutTime(lockoutEndTime)
        
        _uiState.value = _uiState.value.copy(
            isLockedOut = true,
            lockoutEndTime = lockoutEndTime,
            errorMessage = "验证失败次数过多，已锁定5分钟"
        )
        
        auditLogger.logSecurityEvent(
            SecurityEvent.PIN_VERIFICATION_FAILURE,
            "家长验证失败次数过多，已锁定",
            SecuritySeverity.WARNING
        )
        
        checkLockoutStatus()
    }
}

/**
 * 家长认证UI状态
 */
data class ParentAuthUiState(
    val authMethod: AuthMethod = AuthMethod.PIN,
    val pinCode: String = "",
    val mathChallenge: String? = null,
    val mathAnswer: String = "",
    val correctMathAnswer: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val failedAttempts: Int = 0,
    val attemptsRemaining: Int = 5,
    val isLockedOut: Boolean = false,
    val lockoutEndTime: Long = 0
)

/**
 * 扩展函数：获取和设置锁定时间
 */
private fun SecureStorage.getParentAuthLockoutTime(): Long {
    // 这里假设SecureStorage有相应的方法，如果没有，需要添加
    return 0L // 暂时返回0
}

private fun SecureStorage.setParentAuthLockoutTime(time: Long) {
    // 这里假设SecureStorage有相应的方法，如果没有，需要添加
}