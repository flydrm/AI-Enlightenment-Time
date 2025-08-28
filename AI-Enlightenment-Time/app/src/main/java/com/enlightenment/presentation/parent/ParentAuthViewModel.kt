package com.enlightenment.presentation.parent

import androidx.lifecycle.ViewModel
import com.enlightenment.di.DIContainer



class ParentAuthViewModel : ViewModel() {
    private val userPreferences = DIContainer.userPreferences
    
    fun authenticate(password: String): Boolean {
        return password == "1234" // 简化实现
    }
}
