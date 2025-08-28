package com.enlightenment.presentation.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.*



/**
 * 权限处理组件
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionHandler(
    permission: String,
    rationale: String,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val permissionState = rememberPermissionState(
        permission = permission,
        onPermissionResult = { isGranted ->
            if (isGranted) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }
    )
    
    when {
        permissionState.status.isGranted -> {
            // 权限已授予，调用回调
            LaunchedEffect(Unit) {
                onPermissionGranted()
            }
        }
        permissionState.status.shouldShowRationale -> {
            // 需要显示权限说明
            PermissionRationaleDialog(
                permission = permission,
                rationale = rationale,
                onRequestPermission = {
                    permissionState.launchPermissionRequest()
                },
                onDeny = onPermissionDenied
            )
        }
        else -> {
            // 首次请求权限
            LaunchedEffect(Unit) {
                permissionState.launchPermissionRequest()
            }
        }
    }
}
@Composable
private fun PermissionRationaleDialog(
    permission: String,
    rationale: String,
    onRequestPermission: () -> Unit,
    onDeny: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDeny,
        icon = {
            Icon(
                imageVector = when (permission) {
                    android.Manifest.permission.CAMERA -> Icons.Default.PhotoCamera
                    android.Manifest.permission.RECORD_AUDIO -> Icons.Default.Mic
                    else -> Icons.Default.PhotoCamera
                },
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = when (permission) {
                    android.Manifest.permission.CAMERA -> "需要相机权限"
                    android.Manifest.permission.RECORD_AUDIO -> "需要麦克风权限"
                    else -> "需要权限"
                },
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 小熊猫图标和说明
                AnimatedPanda(
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.CenterHorizontally)
                )
                
                Text(
                    text = rationale,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onRequestPermission
            ) {
                Text("好的，授予权限")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDeny
            ) {
                Text("暂时不用")
            }
        }
    )
}
