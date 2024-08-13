package com.example.chatapp.data.model

data class ChatMessage(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
