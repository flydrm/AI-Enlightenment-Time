package com.enlightenment.presentation.camera

import android.Manifest
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.enlightenment.presentation.components.PermissionHandler
import com.enlightenment.presentation.ui.components.PandaMascot
import com.enlightenment.security.AuditLogger
import com.enlightenment.security.UserAction
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

/**
 * 增强版相机界面
 * 儿童友好的拍照体验
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EnhancedCameraScreen(
    onPhotoTaken: (ByteArray) -> Unit,
    onBack: () -> Unit,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    
    // 记录相机访问
    LaunchedEffect(Unit) {
        viewModel.logCameraAccess()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            !cameraPermissionState.status.isGranted -> {
                CameraPermissionScreen(
                    onRequestPermission = {
                        cameraPermissionState.launchPermissionRequest()
                    },
                    onBack = onBack
                )
            }
            uiState.capturedPhoto != null -> {
                PhotoPreviewScreen(
                    photoData = uiState.capturedPhoto!!,
                    isProcessing = uiState.isProcessing,
                    onConfirm = {
                        onPhotoTaken(uiState.capturedPhoto!!)
                    },
                    onRetake = {
                        viewModel.clearCapturedPhoto()
                    }
                )
            }
            else -> {
                CameraPreviewScreen(
                    lifecycleOwner = lifecycleOwner,
                    onPhotoTaken = { data ->
                        viewModel.onPhotoTaken(data)
                    },
                    onBack = onBack,
                    flashMode = uiState.flashMode,
                    onFlashModeChange = viewModel::setFlashMode,
                    isCapturing = uiState.isCapturing
                )
            }
        }
        
        // 错误提示
        uiState.errorMessage?.let { error ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("知道了")
                    }
                }
            ) {
                Text(error)
            }
        }
    }
}

/**
 * 相机权限请求界面
 */
@Composable
private fun CameraPermissionScreen(
    onRequestPermission: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 可爱的相机图标
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "我们需要相机权限",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "小朋友，让我们一起拍照探索世界吧！请让爸爸妈妈帮你打开相机权限。",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // 权限请求按钮
        Button(
            onClick = onRequestPermission,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Camera,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "打开相机",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 返回按钮
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text("返回")
        }
        
        // 底部小熊猫装饰
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            PandaMascot(
                size = 80.dp,
                mood = "friendly"
            )
        }
    }
}

/**
 * 相机预览界面
 */
@Composable
private fun CameraPreviewScreen(
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    onPhotoTaken: (ByteArray) -> Unit,
    onBack: () -> Unit,
    flashMode: FlashMode,
    onFlashModeChange: (FlashMode) -> Unit,
    isCapturing: Boolean
) {
    val cameraViewModel: CameraViewModel = hiltViewModel()
    var cameraFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // 相机预览
        AndroidView(
            factory = { context ->
                PreviewView(context).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { previewView ->
                cameraViewModel.startCamera(lifecycleOwner, previewView, cameraFacing)
            }
        )
        
        // 取景框
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth(0.8f)
                    .border(
                        width = 3.dp,
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.tertiary,
                                MaterialTheme.colorScheme.primary
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
            )
        }
        
        // 顶部控制栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 返回按钮
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回",
                    tint = Color.White
                )
            }
            
            // 闪光灯按钮
            IconButton(
                onClick = {
                    val nextMode = when (flashMode) {
                        FlashMode.OFF -> FlashMode.AUTO
                        FlashMode.AUTO -> FlashMode.ON
                        FlashMode.ON -> FlashMode.OFF
                    }
                    onFlashModeChange(nextMode)
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = when (flashMode) {
                        FlashMode.OFF -> Icons.Default.FlashOff
                        FlashMode.ON -> Icons.Default.FlashOn
                        FlashMode.AUTO -> Icons.Default.FlashAuto
                    },
                    contentDescription = "闪光灯",
                    tint = Color.White
                )
            }
        }
        
        // 底部控制栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 相册按钮（占位）
            Box(modifier = Modifier.size(56.dp))
            
            // 拍照按钮
            CaptureButton(
                onClick = {
                    cameraViewModel.takePicture { result ->
                        when (result) {
                            is CaptureResult.Success -> {
                                onPhotoTaken(result.imageData)
                            }
                            is CaptureResult.Error -> {
                                // 错误已在ViewModel中处理
                            }
                        }
                    }
                },
                enabled = !isCapturing,
                isCapturing = isCapturing
            )
            
            // 切换相机按钮
            IconButton(
                onClick = {
                    cameraFacing = if (cameraFacing == CameraSelector.LENS_FACING_BACK) {
                        CameraSelector.LENS_FACING_FRONT
                    } else {
                        CameraSelector.LENS_FACING_BACK
                    }
                },
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "切换相机",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        // 拍照提示
        if (!isCapturing) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "点击拍照，发现有趣的东西！",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

/**
 * 拍照按钮
 */
@Composable
private fun CaptureButton(
    onClick: () -> Unit,
    enabled: Boolean,
    isCapturing: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isCapturing) 0.8f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    Box(
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .border(
                    width = 4.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
                .background(
                    if (isCapturing) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else Color.Transparent
                )
        )
        
        if (isCapturing) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
        }
    }
}

/**
 * 照片预览界面
 */
@Composable
private fun PhotoPreviewScreen(
    photoData: ByteArray,
    isProcessing: Boolean,
    onConfirm: () -> Unit,
    onRetake: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 照片显示
        AsyncImage(
            model = photoData,
            contentDescription = "拍摄的照片",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
        
        // 半透明遮罩
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )
        
        // 底部操作栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(24.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 重拍按钮
            OutlinedButton(
                onClick = onRetake,
                enabled = !isProcessing,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = BorderStroke(2.dp, Color.White)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("重拍")
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 确认按钮
            Button(
                onClick = onConfirm,
                enabled = !isProcessing,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("使用照片")
                }
            }
        }
        
        // 处理中提示
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(
                        color = Color.Black.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "正在识别图片中的内容...",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// 需要添加的导入
import androidx.camera.core.CameraSelector
import com.enlightenment.multimedia.camera.CaptureResult
import com.enlightenment.multimedia.camera.FlashMode