package com.example.chatapp.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.model.Chat
import com.example.chatapp.data.model.Message
import com.example.chatapp.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    fun sendMessage(messageText: String, senderId: String, receiverId: String) {
        val message = Message(
            senderId = senderId,
            text = messageText,
            timestamp = Date()
        )
        val chatId = generateChatId(senderId, receiverId)

        val chat = Chat(
            id = chatId,
            participants = listOf(senderId, receiverId),
            lastMessage = messageText,
            createdAt = Date()
        )

        viewModelScope.launch {
            chatRepository.saveChatAndMessage(
                chat = chat,
                message = message,
                chatId = chatId
            )
        }
    }

    fun fetchMessages(senderId: String, receiverId: String) {
        val chatRoomId = generateChatId(senderId, receiverId)

        viewModelScope.launch {
            // Fetch messages for the chat room
            chatRepository.getMessages(chatRoomId).collect { result ->
                when {
                    result.isSuccess -> {
                        _messages.value = result.getOrNull() ?: emptyList()
                        Log.d("ChatViewModel", "Messages fetched successfully: ${_messages.value}")
                    }

                    result.isFailure -> {
                        Log.e(
                            "ChatViewModel",
                            "Error fetching messages: ${result.exceptionOrNull()?.message}"
                        )
                    }
                }
            }
        }
    }

    private fun generateChatId(senderId: String, receiverId: String): String {
        val sortedIds = listOf(senderId, receiverId).sorted()
        return sortedIds.joinToString("_")
    }
}