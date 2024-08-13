package com.example.chatapp.data.repository

import com.example.chatapp.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun sendMessage(chatMessage: ChatMessage)
    fun getMessages(): Flow<List<ChatMessage>>
}