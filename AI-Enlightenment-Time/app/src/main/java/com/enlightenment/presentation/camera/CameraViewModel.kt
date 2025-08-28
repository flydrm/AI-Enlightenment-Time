package com.enlightenment.presentation.camera

import android.app.Application
import android.content.Context
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch



class CameraViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _permissionGranted = MutableStateFlow(false)
    val permissionGranted: StateFlow<Boolean> = _permissionGranted
    
    private val _capturedImagePath = MutableStateFlow<String?>(null)
    val capturedImagePath: StateFlow<String?> = _capturedImagePath
    
    fun onPermissionResult(granted: Boolean) {
        _permissionGranted.value = granted
    }
    
    suspend fun takePicture() {
        // 实现拍照逻辑
    }
    
    fun startCamera() {
        // 实现启动相机逻辑
    }
}
