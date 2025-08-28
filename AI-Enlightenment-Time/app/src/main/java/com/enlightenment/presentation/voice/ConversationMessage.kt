package com.enlightenment.presentation.voice


data class ConversationMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
