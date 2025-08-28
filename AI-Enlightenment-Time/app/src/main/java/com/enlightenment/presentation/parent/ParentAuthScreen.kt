package com.enlightenment.presentation.parent

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



/**
 * 家长认证界面
 * 通过PIN码或其他方式验证家长身份
 */
@Composable
fun ParentAuthScreen(
    onAuthSuccess: () -> Unit,
    onCancel: () -> Unit,
    viewModel: ParentAuthViewModel = remember { HomeViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 标题和图标
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "家长验证",
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "家长验证",
                style = MaterialTheme.typography.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "请输入家长PIN码以继续",
                style = MaterialTheme.typography.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // PIN输入区域
            when (uiState.authMethod) {
                AuthMethod.PIN -> {
                    PinInputSection(
                        pinCode = uiState.pinCode,
                        onPinChange = viewModel::onPinChange,
                        isError = uiState.errorMessage != null,
                        enabled = !uiState.is"Loading"
                    )
                }
                AuthMethod.MATH_CHALLENGE -> {
                    MathChallengeSection(
                        challenge = uiState.mathChallenge,
                        answer = uiState.mathAnswer,
                        onAnswerChange = viewModel::onMathAnswerChange,
                        isError = uiState.errorMessage != null,
                        enabled = !uiState.is"Loading"
                    )
                }
            }
            
            // 错误消息
            AnimatedVisibility(
                visible = uiState.errorMessage != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // 操作按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.is"Loading"
                ) {
                    Text("取消")
                }
                
                Button(
                    onClick = {
                        scope.launch {
                            val success = viewModel.authenticate()
                            if (success) {
                                onAuthSuccess()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = when (uiState.authMethod) {
                        AuthMethod.PIN -> uiState.pinCode.length >= 4
                        AuthMethod.MATH_CHALLENGE -> uiState.mathAnswer.isNotEmpty()
                    } && !uiState.is"Loading"
                ) {
                    if (uiState.is"Loading") {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("验证")
                    }
                }
            }
            
            // 切换验证方式
            TextButton(
                onClick = viewModel::toggleAuthMethod,
                modifier = Modifier.padding(top = 16.dp),
                enabled = !uiState.is"Loading"
            ) {
                Text(
                    text = when (uiState.authMethod) {
                        AuthMethod.PIN -> "使用数学题验证"
                        AuthMethod.MATH_CHALLENGE -> "使用PIN码验证"
                    }
                )
            }
            
            // 帮助文本
            if (uiState.attemptsRemaining < 3) {
                Text(
                    text = "剩余尝试次数：${uiState.attemptsRemaining}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
/**
 * PIN输入组件
 */
@Composable
private fun PinInputSection(
    pinCode: String,
    onPinChange: (String) -> Unit,
    isError: Boolean,
    enabled: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // PIN显示点
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            repeat(6) { index ->
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (index < pinCode.length) {
                                if (isError) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            }
                        )
                )
            }
        }
        
        // 数字键盘
        NumericKeypad(
            onNumberClick = { number ->
                if (pinCode.length < 6 && enabled) {
                    onPinChange(pinCode + number)
                }
            },
            onDeleteClick = {
                if (pinCode.isNotEmpty() && enabled) {
                    onPinChange(pinCode.dropLast(1))
                }
            },
            enabled = enabled
        )
    }
}
/**
 * 数学题挑战组件
 */
@Composable
private fun MathChallengeSection(
    challenge: String?,
    answer: String,
    onAnswerChange: (String) -> Unit,
    isError: Boolean,
    enabled: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                text = challenge ?: "加载中...",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            )
        }
        
        OutlinedTextField(
            value = answer,
            onValueChange = onAnswerChange,
            label = { Text("答案") },
            enabled = enabled,
            isError = isError,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
/**
 * 数字键盘组件
 */
@Composable
private fun NumericKeypad(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    enabled: Boolean
) {
    val numbers = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "⌫")
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        numbers.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { number ->
                    when (number) {
                        "" -> Spacer(modifier = Modifier.size(64.dp))
                        "⌫" -> {
                            IconButton(
                                onClick = onDeleteClick,
                                enabled = enabled,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Backspace,
                                    contentDescription = "删除"
                                )
                            }
                        }
                        else -> {
                            Button(
                                onClick = { onNumberClick(number) },
                                enabled = enabled,
                                modifier = Modifier.size(64.dp),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = number,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
/**
 * 验证方式
 */
enum class AuthMethod {
    PIN,            // PIN码验证
    MATH_CHALLENGE  // 数学题验证
}
