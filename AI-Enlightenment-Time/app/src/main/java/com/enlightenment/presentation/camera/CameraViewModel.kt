package com.enlightenment.presentation.camera

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.ai.service.AIService
import com.enlightenment.data.preference.UserPreferences
import com.enlightenment.multimedia.camera.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

/**
 * 相机界面ViewModel
 */

class CameraViewModel constructor(
    private val context: Context,
    private val cameraService: CameraService,
    private val aiService: AIService,
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    companion object {
        private const val TAG = "CameraViewModel"
    }
    
    // 相机状态
    val cameraState = cameraService.cameraState
    
    // 闪光灯模式
    private val _flashMode = MutableStateFlow(FlashMode.OFF)
    val flashMode: StateFlow<FlashMode> = _flashMode.asStateFlow()
    
    // 是否使用后置摄像头
    private val _isBackCamera = MutableStateFlow(true)
    val isBackCamera: StateFlow<Boolean> = _isBackCamera.asStateFlow()
    
    // 最后的拍照结果
    private val _lastCaptureResult = MutableStateFlow<RecognitionResult?>(null)
    val lastCaptureResult: StateFlow<RecognitionResult?> = _lastCaptureResult.asStateFlow()
    
    init {
        // 初始化相机服务
        viewModelScope.launch {
            try {
                cameraService.initialize(context = context)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize camera", e)
            }
        }
    }
    
    /**
     * 开始相机预览
     */
    fun startPreview(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        val lensFacing = if (_isBackCamera.value) {
            androidx.camera.core.CameraSelector.LENS_FACING_BACK
        } else {
            androidx.camera.core.CameraSelector.LENS_FACING_FRONT
        }
        
        cameraService.startPreview(
            lifecycleOwner = lifecycleOwner,
            previewView = previewView,
            lensFacing = lensFacing
        )
    }
    
    /**
     * 拍照并识别
     */
    suspend fun takePicture() {
        try {
            when (val result = cameraService.takePicture()) {
                is CaptureResult.Success -> {
                    // 使用AI服务识别图像
                    val recognitionResults = aiService.imageRecognition.recognizeObjects(
                        imageData = result.imageData
                    )
                    
                    // 获取儿童年龄
                    val childAge = userPreferences.childAge.first()
                    
                    // 生成儿童友好的描述
                    val description = aiService.imageRecognition.generateChildFriendlyDescription(
                        recognitionResults = recognitionResults,
                        childAge = childAge
                    )
                    
                    _lastCaptureResult.value = RecognitionResult(
                        recognizedObjects = recognitionResults,
                        childFriendlyDescription = description
                    )
                    
                    // 使用语音合成朗读描述
                    viewModelScope.launch {
                        try {
                            val audioData = aiService.speechService.textToSpeech(description)
                            
                            // 播放音频
                            playAudio(audioData)
                            
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to synthesize speech", e)
                        }
                    }
                }
                is CaptureResult.Error -> {
                    Log.e(TAG, "Failed to capture image", result.exception)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process image", e)
        }
    }
    
    /**
     * 切换摄像头
     */
    fun switchCamera() {
        _isBackCamera.value = !_isBackCamera.value
        cameraService.switchCamera()
    }
    
    /**
     * 设置闪光灯模式
     */
    fun setFlashMode(mode: FlashMode) {
        _flashMode.value = mode
        cameraService.setFlashMode(mode)
    }
    
    /**
     * 清除识别结果
     */
    fun clearResult() {
        _lastCaptureResult.value = null
    }
    
    /**
     * 播放音频数据
     */
    private suspend fun playAudio(audioData: ByteArray) {
        try {
            // 创建临时文件
            val tempFile = kotlin.io.path.createTempFile(
                directory = context.cacheDir.toPath(),
                prefix = "tts_",
                suffix = ".mp3"
            ).toFile()
            
            // 写入音频数据
            tempFile.writeBytes(audioData)
            
            // 使用MediaPlayer播放
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                prepare()
                
                setOnCompletionListener {
                    release()
                    tempFile.delete()
                }
                
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    release()
                    tempFile.delete()
                    true
                }
            }
            
            mediaPlayer.start()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play audio", e)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        cameraService.release()
    }
}