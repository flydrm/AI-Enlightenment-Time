package com.enlightenment.presentation.camera

import android.Manifest
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enlightenment.multimedia.camera.CameraState
import com.enlightenment.multimedia.camera.FlashMode
import com.enlightenment.presentation.components.AnimatedPanda
import com.enlightenment.presentation.components.PermissionHandler
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

/**
 * 相机界面
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    when {
        cameraPermissionState.status.isGranted -> {
            CameraContent(
                viewModel = viewModel,
                onNavigateBack = onNavigateBack
            )
        }
        else -> {
            PermissionHandler(
                permission = Manifest.permission.CAMERA,
                rationale = "小熊猫需要使用相机来识别物体，帮助你学习新知识哦！",
                onPermissionGranted = {
                    // 权限授予后会自动重组
                },
                onPermissionDenied = {
                    onNavigateBack()
                }
            )
        }
    }
}

@Composable
private fun CameraContent(
    viewModel: CameraViewModel,
    onNavigateBack: () -> Unit
) {
    val cameraState by viewModel.cameraState.collectAsStateWithLifecycle()
    val lastCaptureResult by viewModel.lastCaptureResult.collectAsStateWithLifecycle()
    val flashMode by viewModel.flashMode.collectAsStateWithLifecycle()
    val isBackCamera by viewModel.isBackCamera.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 相机预览
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { previewView ->
                when (cameraState) {
                    is CameraState.Ready, is CameraState.Previewing -> {
                        viewModel.startPreview(lifecycleOwner, previewView)
                    }
                    else -> {}
                }
            }
        )
        
        // 顶部控制栏
        TopCameraControls(
            flashMode = flashMode,
            onFlashModeChange = viewModel::setFlashMode,
            onSwitchCamera = viewModel::switchCamera,
            onNavigateBack = onNavigateBack,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        
        // 底部拍照按钮
        BottomCameraControls(
            onCapture = {
                scope.launch {
                    viewModel.takePicture()
                }
            },
            isCapturing = cameraState is CameraState.Capturing,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        
        // 拍照指引
        if (cameraState is CameraState.Previewing && lastCaptureResult == null) {
            CameraGuide(
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        // 小熊猫助手
        AnimatedPanda(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 100.dp, end = 16.dp)
                .size(60.dp),
            speech = when (cameraState) {
                is CameraState.Previewing -> "对准想要识别的物体，然后按下拍照按钮！"
                is CameraState.Capturing -> "正在拍照..."
                is CameraState.Error -> "出错了，再试一次吧！"
                else -> null
            }
        )
    }
    
    // 处理拍照结果
    lastCaptureResult?.let { result ->
        RecognitionResultDialog(
            result = result,
            onDismiss = viewModel::clearResult,
            onRetry = {
                viewModel.clearResult()
            }
        )
    }
}

@Composable
private fun TopCameraControls(
    flashMode: FlashMode,
    onFlashModeChange: (FlashMode) -> Unit,
    onSwitchCamera: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 返回按钮
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "返回",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 闪光灯控制
            IconButton(
                onClick = {
                    val nextMode = when (flashMode) {
                        FlashMode.OFF -> FlashMode.AUTO
                        FlashMode.AUTO -> FlashMode.ON
                        FlashMode.ON -> FlashMode.OFF
                    }
                    onFlashModeChange(nextMode)
                }
            ) {
                Icon(
                    imageVector = when (flashMode) {
                        FlashMode.OFF -> Icons.Default.FlashOff
                        FlashMode.ON -> Icons.Default.FlashOn
                        FlashMode.AUTO -> Icons.Default.FlashAuto
                    },
                    contentDescription = "闪光灯",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // 切换摄像头
            IconButton(onClick = onSwitchCamera) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "切换摄像头",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun BottomCameraControls(
    onCapture: () -> Unit,
    isCapturing: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        // 拍照按钮
        val scale by animateFloatAsState(
            targetValue = if (isCapturing) 0.9f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        
        IconButton(
            onClick = onCapture,
            enabled = !isCapturing,
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .border(
                    width = 4.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = "拍照",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
private fun CameraGuide(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(200.dp)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // 四个角的指示器
        val cornerSize = 20.dp
        val cornerWidth = 3.dp
        
        // 左上角
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(cornerSize)
                .border(
                    width = cornerWidth,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(topStart = 16.dp)
                )
        )
        
        // 右上角
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(cornerSize)
                .border(
                    width = cornerWidth,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(topEnd = 16.dp)
                )
        )
        
        // 左下角
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(cornerSize)
                .border(
                    width = cornerWidth,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(bottomStart = 16.dp)
                )
        )
        
        // 右下角
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(cornerSize)
                .border(
                    width = cornerWidth,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(bottomEnd = 16.dp)
                )
        )
    }
}

@Composable
private fun RecognitionResultDialog(
    result: RecognitionResult,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "识别结果",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                result.recognizedObjects.forEach { obj ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = obj.label,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "${(obj.confidence * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = result.childFriendlyDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("知道了")
            }
        },
        dismissButton = {
            TextButton(onClick = onRetry) {
                Text("再拍一张")
            }
        }
    )
}

/**
 * 识别结果数据类
 */
data class RecognitionResult(
    val recognizedObjects: List<com.enlightenment.ai.model.RecognitionResult>,
    val childFriendlyDescription: String
)