package com.example.chatapp.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.model.UserProfile
import com.example.chatapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserListViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _userListState = MutableStateFlow<UserListState>(UserListState.Loading)
    val userListState: StateFlow<UserListState> = _userListState

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            _userListState.value = UserListState.Loading
            try {
                val result = userRepository.getAllUsers()
                // Handle the result
                _userListState.value = when {
                    result.isSuccess -> {
                        val users = result.getOrNull()
                        UserListState.Success(users)
                    }
                    else -> {
                        UserListState.Error(
                            result.exceptionOrNull()?.message ?: "Unknown error occurred"
                        )
                    }
                }
            } catch (e: Exception) {
                _userListState.value = UserListState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

}

// UI State
sealed class UserListState {
    object Loading : UserListState()
    data class Success(val users: List<UserProfile>?) : UserListState()
    data class Error(val message: String) : UserListState()
}