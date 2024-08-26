package com.example.chatapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.chatapp.data.repository.AuthRepository
import com.example.chatapp.ui.navigation.AppNavGraph
import com.example.chatapp.ui.theme.ChatAppTheme
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
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
