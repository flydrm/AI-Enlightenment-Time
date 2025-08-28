package com.enlightenment.multimedia.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 相机服务实现
 */
@Singleton
class CameraServiceImpl @Inject constructor() : CameraService {
    
    companion object {
        private const val TAG = "CameraService"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
    
    private val _isCameraAvailable = MutableStateFlow(false)
    override val isCameraAvailable: StateFlow<Boolean> = _isCameraAvailable.asStateFlow()
    
    private val _cameraState = MutableStateFlow<CameraState>(CameraState.Idle)
    override val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()
    
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    
    private var currentLensFacing = CameraSelector.LENS_FACING_BACK
    private var currentFlashMode = FlashMode.OFF
    
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    
    override suspend fun initialize(context: Context) = withContext(Dispatchers.Main) {
        try {
            _cameraState.value = CameraState.Initializing
            
            // 创建输出目录
            outputDirectory = getOutputDirectory(context)
            
            // 创建相机执行器
            cameraExecutor = Executors.newSingleThreadExecutor()
            
            // 获取相机提供者
            cameraProvider = getCameraProvider(context)
            
            // 检查相机是否可用
            _isCameraAvailable.value = hasCamera()
            
            _cameraState.value = CameraState.Ready
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize camera", e)
            _cameraState.value = CameraState.Error(e.message ?: "Unknown error")
            _isCameraAvailable.value = false
        }
    }
    
    override fun startPreview(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        lensFacing: Int
    ) {
        if (_cameraState.value !is CameraState.Ready && _cameraState.value !is CameraState.Previewing) {
            Log.w(TAG, "Camera not ready for preview")
            return
        }
        
        currentLensFacing = lensFacing
        
        try {
            // 解绑所有用例
            cameraProvider?.unbindAll()
            
            // 创建预览用例
            preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
            
            // 创建图像捕获用例
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setFlashMode(currentFlashMode.toCameraXFlashMode())
                .build()
            
            // 创建图像分析用例（可选，用于实时分析）
            imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            
            // 选择相机
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(currentLensFacing)
                .build()
            
            // 绑定用例到相机
            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                imageAnalyzer
            )
            
            _cameraState.value = CameraState.Previewing
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start preview", e)
            _cameraState.value = CameraState.Error(e.message ?: "Failed to start preview")
        }
    }
    
    override fun stopPreview() {
        cameraProvider?.unbindAll()
        _cameraState.value = CameraState.Ready
    }
    
    override suspend fun takePicture(): CaptureResult = withContext(Dispatchers.IO) {
        if (imageCapture == null) {
            return@withContext CaptureResult.Error(IllegalStateException("Camera not initialized"))
        }
        
        _cameraState.value = CameraState.Capturing
        
        try {
            // 创建输出文件
            val photoFile = File(
                outputDirectory,
                SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                    .format(System.currentTimeMillis()) + ".jpg"
            )
            
            // 创建输出选项
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
            
            // 拍照
            val result = suspendCancellableCoroutine<CaptureResult> { continuation ->
                imageCapture?.takePicture(
                    outputFileOptions,
                    cameraExecutor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            val imageData = photoFile.readBytes()
                            continuation.resume(
                                CaptureResult.Success(
                                    imagePath = photoFile.absolutePath,
                                    imageData = imageData
                                )
                            )
                        }
                        
                        override fun onError(exception: ImageCaptureException) {
                            Log.e(TAG, "Photo capture failed", exception)
                            photoFile.delete()
                            continuation.resume(CaptureResult.Error(exception))
                        }
                    }
                )
            }
            
            _cameraState.value = CameraState.Previewing
            return@withContext result
            
        } catch (e: Exception) {
            _cameraState.value = CameraState.Previewing
            return@withContext CaptureResult.Error(e)
        }
    }
    
    override fun switchCamera() {
        currentLensFacing = if (currentLensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        
        // 重新启动预览需要在UI线程上调用startPreview
        stopPreview()
    }
    
    override fun setFlashMode(flashMode: FlashMode) {
        currentFlashMode = flashMode
        imageCapture?.flashMode = flashMode.toCameraXFlashMode()
    }
    
    override fun release() {
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()
        _cameraState.value = CameraState.Idle
    }
    
    private suspend fun getCameraProvider(context: Context): ProcessCameraProvider {
        return suspendCancellableCoroutine { continuation ->
            ProcessCameraProvider.getInstance(context).also { cameraProviderFuture ->
                cameraProviderFuture.addListener({
                    continuation.resume(cameraProviderFuture.get())
                }, ContextCompat.getMainExecutor(context))
            }
        }
    }
    
    private fun hasCamera(): Boolean {
        return cameraProvider?.let { provider ->
            provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ||
                    provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
        } ?: false
    }
    
    private fun getOutputDirectory(context: Context): File {
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, context.packageName).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else context.filesDir
    }
    
    private fun FlashMode.toCameraXFlashMode(): Int = when (this) {
        FlashMode.OFF -> ImageCapture.FLASH_MODE_OFF
        FlashMode.ON -> ImageCapture.FLASH_MODE_ON
        FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
    }
}