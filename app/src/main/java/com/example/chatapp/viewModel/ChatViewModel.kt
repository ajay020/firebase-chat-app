package com.example.chatapp.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.model.ChatMessage
import com.example.chatapp.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    init {
        fetchMessages()
    }

    fun sendMessage(message: String, userId: String, userName: String) {
        val chatMessage = ChatMessage(
            userId = userId,
            userName = userName,
            message = message
        )
        chatRepository.sendMessage(chatMessage)
    }

    private fun fetchMessages() {
        viewModelScope.launch {
            chatRepository.getMessages().collect {
                _messages.value = it
            }
        }
    }
}
