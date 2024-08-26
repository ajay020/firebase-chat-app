package com.example.chatapp.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.chatapp.data.model.UserProfile
import com.example.chatapp.ui.components.ProgressIndicator
import com.example.chatapp.viewModel.UserListState
import com.example.chatapp.viewModel.UserListViewModel
import com.google.firebase.auth.FirebaseUser

@Composable
fun UserListScreen(
    userListViewModel: UserListViewModel = hiltViewModel(),
    navigateToChatScreen: (String, String, String?, Boolean, Long?) -> Unit,
    currentUser: FirebaseUser?,
    navigateToProfile: () -> Unit,
) {
    val userListState by userListViewModel.userListState.collectAsState()

    Scaffold(
        topBar = {
            UserListTopBar(
                navigateToProfile = navigateToProfile
            )
        }
    ) {
        UserListScreenContent(
            modifier = Modifier.padding(it),
            userListState = userListState,
            navigateToChatScreen = navigateToChatScreen,
            currentUser = currentUser
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListTopBar(
    navigateToProfile: () -> Unit
) {
    TopAppBar(
        title = {
            Text(text = "Users")
        },
        actions = {
            IconButton(onClick = navigateToProfile) {
                Icon(imageVector = Icons.Filled.Settings, contentDescription = "Settings")
            }
        },
    )
}

@Composable
fun UserListScreenContent(
    modifier: Modifier = Modifier,
    userListState: UserListState,
    navigateToChatScreen: (String, String, String?, Boolean, Long?) -> Unit,
    currentUser: FirebaseUser?
) {
    when (userListState) {
        is UserListState.Loading -> ProgressIndicator()
        is UserListState.Success -> userListState.users?.let {
            UserList(
                modifier = modifier,
                users = it,
                currentUser = currentUser,
                navigateToChatScreen = navigateToChatScreen
            )
        }

        is UserListState.Error -> Text("Error: ${userListState.message}")
    }
}

@Composable
fun UserList(
    modifier: Modifier = Modifier,
    users: List<UserProfile>,
    currentUser: FirebaseUser?,
    navigateToChatScreen: (String, String, String? , Boolean, Long?) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(users) { user ->
            // Exclude the current user from the list
            if (user.uid == currentUser?.uid) return@items
            UserItem(
                user,
                navigateToChatScreen
            )
        }
    }
}

@Composable
fun UserItem(user: UserProfile, navigateToChatScreen: (String, String, String? , Boolean, Long?) -> Unit) {
    val imageUrl = user.profilePictureUrl.takeIf { !it.isNullOrEmpty() }

    val painter = rememberAsyncImagePainter(
        model = imageUrl,
        placeholder = painterResource(id = R.drawable.ic_person2),
        error = painterResource(id = R.drawable.ic_person2),
        onError = {
            Log.d("UserListScreen", "Error loading image: ${it.result.throwable.message}")
        }
    )

    Row(
        modifier = Modifier
            .padding(8.dp)
            .clickable {
                navigateToChatScreen(user.uid, user.displayName, user.profilePictureUrl, user.isOnline, user.lastSeen)
            }
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
        Text(text = user.displayName)
    }
}

@Preview(showBackground = true)
@Composable
private fun UserItemPreview() {
    val user = UserProfile(
        displayName = "John Doe",
        profilePictureUrl = "https://firebasestorage.googleapis.com/v0/b/chat-app-36e40.appspot.com/o/profile_pictures%2FTjB7sQXoHjZCCKrJyxvAJkyktdf2.jpg?alt=media&token=145374a9-f046-4e99-bd1b-54f7237f114c"
    )
    UserItem(
        user = user,
        navigateToChatScreen = {_, _, _ , _, _ -> }
    )
}