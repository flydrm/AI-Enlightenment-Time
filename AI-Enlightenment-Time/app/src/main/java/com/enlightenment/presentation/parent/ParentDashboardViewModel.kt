package com.enlightenment.presentation.parent

import androidx.lifecycle.ViewModel
import com.enlightenment.di.DIContainer
import com.enlightenment.domain.model.WeeklyProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow



class ParentDashboardViewModel : ViewModel() {
    private val userProgressRepository = DIContainer.userProgressRepository
    
    private val _weeklyProgress = MutableStateFlow(WeeklyProgress())
    val weeklyProgress: StateFlow<WeeklyProgress> = _weeklyProgress
}
