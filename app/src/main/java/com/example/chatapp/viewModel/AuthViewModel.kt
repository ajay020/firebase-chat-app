package com.example.chatapp.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.model.UserProfile
import com.example.chatapp.data.repository.AuthRepository
import com.example.chatapp.data.repository.ProfileRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    init {
        checkIfUserIsSignedIn()
    }

    private fun checkIfUserIsSignedIn() {
        authRepository.getCurrentUser()?.let { user ->
            _authState.value = AuthState.Success(user)
        }
    }

    fun signUpWithEmailPassword(displayName: String, email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                authRepository.signUpWithEmailPassword(displayName, email, password)
                    .collect { result ->
                        if (result.isSuccess) {
                            // If the sign-up is successful, update the user's display name
                            val firebaseUser = result.getOrNull()
                            if (firebaseUser != null) {
                                // Ensure the updateUserProfile call is properly awaited
                                val updateResult = profileRepository.updateUserProfile(
                                    UserProfile(
                                        uid = firebaseUser.uid,
                                        displayName = generateDisplayName(email)
                                    )
                                )
                                // Check if the update was successful
                                if (updateResult.isSuccess) {
                                    _authState.value = AuthState.Success(firebaseUser)
                                } else {
                                    _authState.value =
                                        AuthState.Error("Failed to update user profile: ${updateResult.exceptionOrNull()?.message}")
                                }
                            } else {
                                _authState.value =
                                    AuthState.Error("Authentication failed. User is null.")
                            }
                        } else {
                            _authState.value = AuthState.Error(
                                result.exceptionOrNull()?.message ?: "Unknown error"
                            )
                        }
                    }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An error occurred")
            }
        }
    }


//    fun signUpWithEmailPassword(displayName: String, email: String, password: String) {
//        _authState.value = AuthState.Loading
//        viewModelScope.launch {
//
//            authRepository.signUpWithEmailPassword(displayName, email, password).collect { result ->
//                _authState.value = if (result.isSuccess) {
//                    // If the sign-up is successful, update the user's display name
//                    val firebaseUser = result.getOrNull()
//                    if (firebaseUser != null) {
//                        profileRepository.updateUserProfile(
//                            UserProfile(
//                                uid = firebaseUser.uid,
//                                displayName = generateDisplayName(email)
//                            )
//                        )
//                    }
//                    AuthState.Success(result.getOrNull())
//                } else {
//                    AuthState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
//                }
//            }
//        }
//    }

    fun signInWithEmailPassword(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.signInWithEmailPassword(email, password).collect { result ->
                _authState.value = if (result.isSuccess) {
                    AuthState.Success(result.getOrNull())
                } else {
                    AuthState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                }
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _authState.value = AuthState.Success(null)
    }

    fun getCurrentUser(): FirebaseUser? = authRepository.getCurrentUser()

    private fun generateDisplayName(email: String): String {
        email.split("@").let {
            return it[0]
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser?) : AuthState()
    data class Error(val message: String) : AuthState()
}