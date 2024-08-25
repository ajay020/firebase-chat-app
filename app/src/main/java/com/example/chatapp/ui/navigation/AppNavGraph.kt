package com.example.chatapp.ui.navigation

import android.util.Base64
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.chatapp.ui.screens.AuthScreen
import com.example.chatapp.ui.screens.ChatScreen
import com.example.chatapp.ui.screens.ProfileScreen
import com.example.chatapp.ui.screens.UserListScreen
import com.example.chatapp.viewModel.AuthViewModel
import java.net.URLDecoder
import java.net.URLEncoder

object Screens {
    const val AUTH = "auth"
    const val CHAT = "chat"
    const val PROFILE = "profile"
    const val USER_LIST = "user_list"
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
                    navController.navigate(Screens.USER_LIST)
                }
            )
        }

        composable(
            "${Screens.CHAT}/{userId}/{userDisplayName}/{userPhotoUrl}/{isOnline}/{lastSeen}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("userDisplayName") { type = NavType.StringType },
                navArgument("userPhotoUrl") { type = NavType.StringType; nullable = true },
                navArgument("isOnline") { type = NavType.BoolType },
                navArgument("lastSeen") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")!!
            val userDisplayName = backStackEntry.arguments?.getString("userDisplayName")!!
            val userPhotoUrl = backStackEntry.arguments?.getString("userPhotoUrl")?.let {
                String(
                    Base64.decode(it, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP),
                    Charsets.UTF_8
                )
            }
            val isOnline = backStackEntry.arguments?.getBoolean("isOnline")!!
            val lastSeen = backStackEntry.arguments?.getString("lastSeen")?.toLongOrNull()

            Log.d("AppNav", "$isOnline $lastSeen")

            authViewModel.getCurrentUser()?.let { it1 ->
                ChatScreen(
                    receiverUserId = userId,
                    receiverUserDisplayName = userDisplayName,
                    receiverUserPhotoUrl = userPhotoUrl,
                    isOnline = isOnline,
                    lastSeen = lastSeen,
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
                    navController.popBackStack()
                }
            )
        }

        composable(Screens.USER_LIST) {
            UserListScreen(
                currentUser = authViewModel.getCurrentUser(),
                navigateToChatScreen = { uid, displayName, photoUrl, isOnline, lastseen ->

                    // Encode the photo URL using Base64
                    val encodedPhotoUrl = photoUrl?.let {
                        Base64.encodeToString(
                            it.toByteArray(Charsets.UTF_8),
                            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
                        )
                    }

                    // Navigate to the chat screen with the user's ID and display name
                    navController.navigate(
                        "${Screens.CHAT}/${uid}/${displayName}/${encodedPhotoUrl}/${isOnline}/${lastseen?.toString()}"
                    )
                }
            )
        }
    }
}

