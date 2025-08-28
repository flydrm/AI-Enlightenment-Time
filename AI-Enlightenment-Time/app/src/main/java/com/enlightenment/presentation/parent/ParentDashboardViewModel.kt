package com.enlightenment.presentation.parent

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.di.DIContainer
import com.enlightenment.domain.model.WeeklyProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch



class ParentDashboardViewModel : ViewModel() {
    private val userProgressRepository = DIContainer.userProgressRepository
    
    private val _weeklyProgress = MutableStateFlow(WeeklyProgress())
    val weeklyProgress: StateFlow<WeeklyProgress> = _weeklyProgress
    
    private val _uiState = MutableStateFlow(ParentDashboardUiState())
    val uiState: StateFlow<ParentDashboardUiState> = _uiState
}

data class ParentDashboardUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)
