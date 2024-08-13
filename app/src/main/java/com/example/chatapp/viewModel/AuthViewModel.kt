package com.example.chatapp.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
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

fun signUpWithEmailPassword( displayName:String,  email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.signUpWithEmailPassword( displayName, email, password).collect { result ->
                _authState.value = if (result.isSuccess) {
                    AuthState.Success(result.getOrNull())
                } else {
                    AuthState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                }
            }
        }
    }

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
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser?) : AuthState()
    data class Error(val message: String) : AuthState()
}