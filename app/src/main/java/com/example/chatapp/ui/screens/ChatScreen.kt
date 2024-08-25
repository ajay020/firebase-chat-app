package com.example.chatapp.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.chatapp.R
import com.example.chatapp.data.model.Message
import com.example.chatapp.utils.Utils
import com.example.chatapp.viewModel.ChatViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseUserMetadata
import com.google.firebase.auth.MultiFactor
import com.google.firebase.auth.MultiFactorInfo
import com.google.firebase.auth.UserInfo
import java.util.Date

@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel = hiltViewModel(),
    currentUser: FirebaseUser,
    signOut: () -> Unit,
    navigateToProfile: () -> Unit,
    receiverUserId: String,
    receiverUserDisplayName: String,
    receiverUserPhotoUrl: String?,
    isOnline: Boolean,
    lastSeen: Long?
) {
    val messages by chatViewModel.messages.collectAsState()

    LaunchedEffect(currentUser.uid, receiverUserId) {
        chatViewModel.fetchMessages(senderId = currentUser.uid, receiverId = receiverUserId)
    }

    Scaffold(
        topBar = {
            ChatTopBar(
                modifier = Modifier,
                receiverUserNme = receiverUserDisplayName,
                receiverUserPhotoUrl = receiverUserPhotoUrl,
                isOnline = isOnline,
                lastSeen = lastSeen,
                signOut = signOut,
                navigateToProfile = navigateToProfile
            )
        }
    ) {
        ChatScreenContent(
            modifier = Modifier
                .padding(it)
                .imePadding(),// This ensures the content will respect the keyboard height,
            messages = messages,
            currentUser = currentUser,
            sendNewMessage = { newMessage ->
                chatViewModel.sendMessage(
                    messageText = newMessage,
                    senderId = currentUser.uid,
                    receiverId = receiverUserId
                )
            }
        )
    }
}

@Composable
fun ChatScreenContent(
    modifier: Modifier = Modifier,
    messages: List<Message>,
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

    Column(
        modifier = modifier
            .fillMaxSize()

    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            reverseLayout = false
        ) {
            items(messages) { message ->
                ChatMessageItem(
                    message = message,
                    isCurrentUser = message.senderId == currentUser.uid
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
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
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Message"
                )
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: Message, isCurrentUser: Boolean) {
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
            text = message.text,
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
    receiverUserNme: String = "",
    receiverUserPhotoUrl: String? = null,
    isOnline: Boolean = false,
    lastSeen: Long? = null,
    signOut: () -> Unit = {},
    navigateToProfile: () -> Unit
) {
    val imageUrl = receiverUserPhotoUrl?.takeIf { it.isNotEmpty() }

    val painter = rememberAsyncImagePainter(
        model = imageUrl,
        placeholder = painterResource(id = R.drawable.ic_person2),
        error = painterResource(id = R.drawable.ic_person2),
        onError = {
            Log.d("ChatScreen", "Error loading image: ${it.result.throwable.message}")
        }
    )

    val lastSeenFormatted = Utils().formatLastSeen(lastSeen ?: 0)

    TopAppBar(
        title = {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile picture
                Image(
                    painter = painter,
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .border(1.dp, Color.Gray, CircleShape)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.padding(8.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = receiverUserNme,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (isOnline)
                            "Online"
                        else "Last seen: $lastSeenFormatted",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = navigateToProfile) {
                Icon(imageVector = Icons.Filled.Settings, contentDescription = "Send Message")
            }
        },
        modifier = modifier.background(Color.Gray),
        colors = TopAppBarColors(
            containerColor = Color.Cyan,
            titleContentColor = Color.Black,
            navigationIconContentColor = Color.Black,
            actionIconContentColor = Color.Black,
            scrolledContainerColor = Color.Cyan
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    val messages = listOf(
        Message(senderId = "", text = "Hello", timestamp = null)
    )

    ChatTopBar(
        receiverUserNme = "John Doe",
        receiverUserPhotoUrl = "",
        signOut = {},
        navigateToProfile = {},
        isOnline = false,
        lastSeen = 1724592794226,
        modifier = Modifier
    )


}

@Preview(showBackground = true)
@Composable
fun ChatScreenContentPreview() {
//    val fakeUser = FakeFirebaseUser("fakeUserId")
//    val sampleMessages = listOf(
//        Message(senderId = "fakeUserId", text = "Hello!", timestamp = Date()),
//        Message(senderId = "otherUserId", text = "Hi there!", timestamp = Date())
//    )
//
//    ChatScreenContent(
//        messages = sampleMessages,
//        currentUser = fakeUser,
//        sendNewMessage = {}
//    )
}

