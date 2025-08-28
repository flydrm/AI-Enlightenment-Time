package com.enlightenment.presentation.parent

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.di.DIContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch



class ParentAuthViewModel : ViewModel() {
    private val userPreferences = DIContainer.userPreferences
    
    private val _uiState = MutableStateFlow(ParentAuthUiState())
    val uiState: StateFlow<ParentAuthUiState> = _uiState
    
    fun authenticate(password: String): Boolean {
        return password == "1234"
    }
    
    fun onPinChange(pin: String) {
        _uiState.value = _uiState.value.copy(pinCode = pin)
    }
    
    fun onMathAnswerChange(answer: String) {
        _uiState.value = _uiState.value.copy(mathAnswer = answer)
    }
    
    fun toggleAuthMethod() {
        _uiState.value = _uiState.value.copy(
            authMethod = if (_uiState.value.authMethod == AuthMethod.PIN) {
                AuthMethod.MATH_CHALLENGE
            } else {
                AuthMethod.PIN
            }
        )
    }
}

data class ParentAuthUiState(
    val authMethod: AuthMethod = AuthMethod.PIN,
    val pinCode: String = "",
    val mathChallenge: String = "5 + 3 = ?",
    val mathAnswer: String = "",
    val isError: Boolean = false,
    val isLoading: Boolean = false
)
