package com.enlightenment.presentation.parent

@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.enlightenment.presentation.components.AnimatedPanda
import com.enlightenment.presentation.components.PandaMood

@Composable
fun ParentAuthScreen(
    onAuthSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val viewModel = remember { ParentAuthViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedPanda(
            mood = if (uiState.isError) PandaMood.THINKING else PandaMood.HAPPY,
            size = 120.dp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "家长验证",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "请输入家长密码以继续",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = uiState.pinCode,
            onValueChange = viewModel::onPinChange,
            label = { Text("密码") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            isError = uiState.isError,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(onClick = onBack) {
                Text("返回")
            }
            
            Button(
                onClick = {
                    if (viewModel.authenticate(uiState.pinCode)) {
                        onAuthSuccess()
                    }
                }
            ) {
                Text("确认")
            }
        }
    }
}
