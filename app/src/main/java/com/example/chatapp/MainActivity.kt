package com.example.chatapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatapp.data.repository.AuthRepository
import com.example.chatapp.ui.navigation.AppNavGraph
import com.example.chatapp.ui.screens.AuthScreen
import com.example.chatapp.ui.screens.ChatScreen
import com.example.chatapp.ui.theme.ChatAppTheme
import com.example.chatapp.viewModel.AuthViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChatAppTheme {
                AppNavGraph()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("MainActivity", "onStart called")

        authRepository.updateUserStatus(true)
    }

    override fun onStop() {
        super.onStop()
        Log.d("MainActivity", "onStop called")
        authRepository.updateUserStatus(false)
    }
}
