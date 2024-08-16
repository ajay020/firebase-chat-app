package com.example.chatapp.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.chatapp.R
import com.example.chatapp.data.model.UserProfile
import com.example.chatapp.ui.components.ProgressIndicator
import com.example.chatapp.viewModel.ProfileState
import com.example.chatapp.viewModel.ProfileViewModel

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    signOut: () -> Unit,
    navigateToChat: () -> Unit
) {
    val profileState by profileViewModel.profileState.collectAsState()

    Scaffold(
        topBar = {
            ProfileScreenTopBar(
                navigateToChat = navigateToChat
            )
        }
    ) {
        ProfileScreenContent(
            modifier = Modifier.padding(it),
            profileState = profileState,
            updateProfile = { displayName, profilePictureUri ->
                profileViewModel.updateProfile(displayName, profilePictureUri)
            },
            signOut = signOut
        )
    }
    Log.d("ProfileScreenContent1", "ProfileScreen compose called")

}

@Composable
fun ProfileScreenContent(
    modifier: Modifier = Modifier,
    profileState: ProfileState = ProfileState.Loading,
    updateProfile: (String, Uri?) -> Unit = { _, _ -> },
    signOut: () -> Unit,
) {
    var displayName by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            // Handle the selected image URI
            if (uri != null) {
                selectedImageUri = uri
            }
        }
    )

    val painter = rememberAsyncImagePainter(
        model = selectedImageUri ?: "",
        placeholder = painterResource(id = R.drawable.ic_person),
        error = painterResource(id = R.drawable.ic_error),
        onError = {
            Log.d("ProfileScreenContent", "Error loading image: ${it.result.throwable.message}")
        }
    )

    LaunchedEffect(profileState) {
        if (profileState is ProfileState.Success) {
            val userProfile = profileState.userProfile
            displayName = userProfile?.displayName ?: ""
            selectedImageUri = userProfile?.profilePictureUrl?.toUri()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        if (profileState is ProfileState.Loading) {
            ProgressIndicator()
        }

        if (profileState is ProfileState.Error) {
            Text("Error: ${(profileState).message}")
        }

        // Display and allow changing the profile picture
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painter,
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .border(1.dp, Color.Gray, CircleShape)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                modifier = Modifier.clickable {
                    launcher.launch("image/*")
                },
                text = "Select an image"
            )
        }

        Spacer(modifier = Modifier.padding(8.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Full Name") }
        )

        Spacer(modifier = Modifier.padding(8.dp))

        Button(
            onClick = { updateProfile(displayName, selectedImageUri) },
            modifier = Modifier.fillMaxWidth(),
            shape = RectangleShape
        ) {
            Text("Save")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { signOut() },
            modifier = Modifier.fillMaxWidth(),
            shape = RectangleShape
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
            Text("Sign Out")
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenTopBar(modifier: Modifier = Modifier, navigateToChat: () -> Unit) {
    TopAppBar(
        title = {
            Text(text = "Edit Profile")
        },
        modifier = modifier
            .fillMaxWidth(),
        navigationIcon = {
            IconButton(onClick = navigateToChat) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenContentPreview() {
    ProfileScreenTopBar(navigateToChat = { })

    val userProfile = UserProfile(uid = "1", displayName = "John Doe")
    ProfileScreenContent(
        updateProfile = { _, _ -> },
        signOut = {}
    )
}