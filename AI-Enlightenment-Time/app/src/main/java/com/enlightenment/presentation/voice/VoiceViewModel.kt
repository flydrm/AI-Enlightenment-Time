package com.enlightenment.presentation.voice

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch



class VoiceViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording
    
    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText
    
    fun startRecording() {
        _isRecording.value = true
    }
    
    fun stopRecording() {
        _isRecording.value = false
    }
}
