package com.enlightenment.multimedia.camera

import android.content.Context
import android.net.Uri
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine



/**
 * 相机管理器
 * 负责相机的初始化、预览、拍照等功能
 */
class CameraManager(
    private val context: Context
) {
    private var camera: Camera? = null
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    
    /**
     * 初始化相机
     */
    suspend fun initializeCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        lensFacing: Int = CameraSelector.LENS_FACING_BACK
    ) = suspendCancellableCoroutine<Unit> { continuation ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                
                // 构建预览
                preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                
                // 构建图片捕获
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                
                // 选择相机
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()
                
                // 解绑所有用例
                cameraProvider?.unbindAll()
                
                // 绑定用例到相机
                camera = cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                
                continuation.resume(Unit)
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }, ContextCompat.getMainExecutor(context))
    }
    
    /**
     * 拍照
     */
    suspend fun takePicture(): Uri = suspendCancellableCoroutine { continuation ->
        val photoFile = createPhotoFile()
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        
        imageCapture?.takePicture(
            outputFileOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    continuation.resume(savedUri)
                }
                
                override fun onError(exception: ImageCaptureException) {
                    continuation.resumeWithException(exception)
                }
            }
        )
    }
    
    /**
     * 切换相机（前置/后置）
     */
    suspend fun switchCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        val currentLensFacing = camera?.cameraInfo?.let { cameraInfo ->
            when {
                cameraInfo.hasCamera(CameraSelector.LENS_FACING_BACK) -> CameraSelector.LENS_FACING_BACK
                cameraInfo.hasCamera(CameraSelector.LENS_FACING_FRONT) -> CameraSelector.LENS_FACING_FRONT
                else -> CameraSelector.LENS_FACING_BACK
            }
        } ?: CameraSelector.LENS_FACING_BACK
        
        val newLensFacing = if (currentLensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        
        initializeCamera(lifecycleOwner, previewView, newLensFacing)
    }
    
    /**
     * 检查是否有闪光灯
     */
    fun hasFlashUnit(): Boolean {
        return camera?.cameraInfo?.hasFlashUnit() ?: false
    }
    
    /**
     * 切换闪光灯
     */
    fun toggleFlash(enabled: Boolean) {
        camera?.cameraControl?.enableTorch(enabled)
    }
    
    /**
     * 释放资源
     */
    fun release() {
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()
        camera = null
        preview = null
        imageCapture = null
        cameraProvider = null
    }
    
    /**
     * 创建照片文件
     */
    private fun createPhotoFile(): File {
        val photoDir = File(context.filesDir, "photos").apply {
            if (!exists()) mkdirs()
        }
        
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(photoDir, "IMG_$timeStamp.jpg")
    }
    
    /**
     * 获取照片保存目录
     */
    fun getPhotosDirectory(): File {
        return File(context.filesDir, "photos").apply {
            if (!exists()) mkdirs()
        }
    }
    
    /**
     * 删除照片
     */
    fun deletePhoto(uri: Uri): Boolean {
        return try {
            val file = File(uri.path ?: return false)
            file.delete()
        } catch (e: Exception) {
            false
        }
    }
}
