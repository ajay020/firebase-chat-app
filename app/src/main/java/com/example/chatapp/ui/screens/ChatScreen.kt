package com.example.chatapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.chatapp.data.model.ChatMessage
import com.example.chatapp.viewModel.ChatViewModel
import com.google.firebase.auth.FirebaseUser

@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel = hiltViewModel(),
    currentUser: FirebaseUser,
    signOut: () -> Unit,
    navigateToProfile: () -> Unit
) {
    val messages by chatViewModel.messages.collectAsState()

    Scaffold(
        topBar = {
            ChatTopBar( signOut = signOut, navigateToProfile = navigateToProfile )
        }
    ) {
        ChatScreenContent(
            modifier = Modifier
                .padding(
                    PaddingValues(
                        bottom = it.calculateBottomPadding(),
                        top = it.calculateTopPadding()
                    )
                ),
            messages = messages,
            currentUser = currentUser,
            sendNewMessage = { newMessage ->
                chatViewModel.sendMessage(
                    message = newMessage,
                    userId = currentUser.uid,
                    userName = currentUser.displayName ?: "Anonymous"
                )
            }
        )
    }
}

@Composable
fun ChatScreenContent(
    modifier: Modifier = Modifier,
    messages: List<ChatMessage>,
    currentUser: FirebaseUser,
    sendNewMessage: (String) -> Unit = {},
) {
    var newMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to the bottom when a new message is sent
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            reverseLayout = false
        ) {
            items(messages) { message ->
                ChatMessageItem(
                    message = message,
                    isCurrentUser = message.userId == currentUser.uid
                )
            }
        }
        Row(
            modifier = Modifier.run {
                fillMaxWidth()
                    .padding(8.dp)
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = newMessage,
                onValueChange = { newMessage = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message") }
            )
            IconButton(onClick = {
                if (newMessage.isNotBlank()) {
                    sendNewMessage(newMessage)
                    newMessage = ""
                }
            }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send Message")
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage, isCurrentUser: Boolean) {
    val alignment = if (isCurrentUser) Alignment.TopEnd else Alignment.TopStart
    val backgroundColor =
        if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        contentAlignment = alignment
    ) {
        Text(
            text = message.message,
            color = Color.White,
            modifier = Modifier
                .background(backgroundColor)
                .padding(8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    modifier: Modifier = Modifier,
    signOut: () -> Unit = {},
    navigateToProfile: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(text = "Chat")
        },
        actions = {
            IconButton(onClick = navigateToProfile) {
                Icon(imageVector = Icons.Filled.Settings, contentDescription = "Send Message")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    val messages = listOf(
        ChatMessage(message = "Hello", userId = "1", userName = "Alice"),
        ChatMessage(message = "Hi there", userId = "2", userName = "Bob")
    )

    ChatMessageItem(message = messages.first(), isCurrentUser = false)
}