package com.example.chatapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.chatapp.viewModel.AuthViewModel

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel(),
    navigateToAuthScreen: () -> Unit
) {



    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                authViewModel.signOut()
                navigateToAuthScreen()
            }) {
            Text(text = "Logout")
        }
    }
}