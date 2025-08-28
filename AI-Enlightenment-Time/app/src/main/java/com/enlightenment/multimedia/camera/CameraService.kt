package com.enlightenment.multimedia.camera

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow



/**
 * 相机服务接口
 */
interface CameraService {
    /**
     * 相机是否可用
     */
    val isCameraAvailable: StateFlow<Boolean>
    
    /**
     * 当前相机状态
     */
    val cameraState: StateFlow<CameraState>
    
    /**
     * 初始化相机
     */
    suspend fun initialize(context: Context)
    
    /**
     * 开始相机预览
     */
    fun startPreview(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        lensFacing: Int = CameraSelector.LENS_FACING_BACK
    )
    
    /**
     * 停止相机预览
     */
    fun stopPreview()
    
    /**
     * 拍照
     * @return 拍摄的照片文件路径
     */
    suspend fun takePicture(): CaptureResult
    
    /**
     * 切换前后摄像头
     */
    fun switchCamera()
    
    /**
     * 设置闪光灯模式
     */
    fun setFlashMode(flashMode: FlashMode)
    
    /**
     * 释放相机资源
     */
    fun release()
}
/**
 * 相机状态
 */
sealed class CameraState {
    object Idle : CameraState()
    object Initializing : CameraState()
    object Ready : CameraState()
    object Previewing : CameraState()
    object Capturing : CameraState()
    data class Error(val message: String) : CameraState()
}
/**
 * 拍照结果
 */
sealed class CaptureResult {
    data class Success(val imagePath: String, val imageData: ByteArray) : CaptureResult()
    data class Error(val exception: Exception) : CaptureResult()
}
/**
 * 闪光灯模式
 */
enum class FlashMode {
    OFF,
    ON,
    AUTO
}
