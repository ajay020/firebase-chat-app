package com.example.chatapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatapp.ui.screens.AuthScreen
import com.example.chatapp.ui.screens.ChatScreen

object Screens {
    const val AUTH = "auth"
    const val CHAT = "chat"
}

@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screens.AUTH) {

        composable(Screens.AUTH) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(Screens.CHAT)
                }
            )
        }

        composable(Screens.CHAT) {
            ChatScreen(
                navigateToAuthScreen = {
                    navController.navigate(Screens.AUTH)
                }
            )
        }
    }
}

