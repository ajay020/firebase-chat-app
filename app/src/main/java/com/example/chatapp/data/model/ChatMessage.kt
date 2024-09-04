package com.example.chatapp.data.model
import java.util.Date

data class Chat(
    val id: String = "", // Document ID (e.g., "user1_user2")
    val participants: List<String> = emptyList(),
    var lastMessage: String? = null,
    val createdAt: Date? = null,
)

data class Message(
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Date? = null,
    val imageUrl: String? = null,

)