package com.example.chatapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatapp.ui.screens.AuthScreen
import com.example.chatapp.ui.screens.ChatScreen
import com.example.chatapp.ui.screens.ProfileScreen
import com.example.chatapp.viewModel.AuthViewModel

object Screens {
    const val AUTH = "auth"
    const val CHAT = "chat"
    const val PROFILE = "profile"
}

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel()
) {
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
            authViewModel.getCurrentUser()?.let { it1 ->
                ChatScreen(
                    currentUser = it1,
                    signOut = {
                        authViewModel.signOut()
                        navController.navigate(Screens.AUTH)
                    },
                    navigateToProfile = {
                        navController.navigate(Screens.PROFILE)
                    }
                )
            }
        }

        composable(Screens.PROFILE) {
            ProfileScreen(
                signOut = {
                    authViewModel.signOut()
                    navController.navigate(Screens.AUTH)
                },
                navigateToChat = {
                    navController.navigate(Screens.CHAT)
                }
            )
        }
    }
}

