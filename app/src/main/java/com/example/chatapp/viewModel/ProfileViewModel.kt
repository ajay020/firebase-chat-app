package com.example.chatapp.viewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.model.UserProfile
import com.example.chatapp.data.repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val firebaseAuth: FirebaseAuth,
    private val storage: FirebaseStorage
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            val result = profileRepository.getUserProfile()
            _profileState.value = when {
                result.isSuccess -> {
                    val userProfile = result.getOrNull()
                    if (userProfile != null) {
                        Log.d("ProfileModel", "loadProfile: ${userProfile.uid}")
                    }
                    ProfileState.Success(userProfile)
                }

                else -> {
                    Log.d("ProfileModel", "loadProfile: ${result.exceptionOrNull()?.message}")
                    ProfileState.Error(
                        result.exceptionOrNull()?.message ?: "Error loading profile"
                    )
                }
            }
        }
    }

    fun updateProfile(displayName: String, newProfilePictureUri: Uri?) {
        val userId = firebaseAuth.currentUser?.uid
            ?: return
        val userProfile = UserProfile(
            uid = userId,
            displayName = displayName,
            profilePictureUrl = newProfilePictureUri?.toString()
        )
        val currentUserProfile = (profileState.value as? ProfileState.Success)?.userProfile

        viewModelScope.launch {
            _profileState.value = ProfileState.Loading

            if (newProfilePictureUri != null && newProfilePictureUri.toString() != currentUserProfile?.profilePictureUrl) {
                // Delete the old profile picture if it exists
                currentUserProfile?.profilePictureUrl?.let { oldUrl ->
                    deleteOldProfilePicture(oldUrl)
                }

                // Upload the new image
                val uri = uploadImageToStorage(newProfilePictureUri, userId)
                Log.d("ProfileViewModel", "saveProfile: $uri")
                userProfile.profilePictureUrl = uri
            }

            val result = profileRepository.updateUserProfile(userProfile)
            _profileState.value = when {
                result.isSuccess -> ProfileState.Success(userProfile)
                else -> ProfileState.Error(
                    result.exceptionOrNull()?.message ?: "Error saving profile"
                )
            }
        }
    }

    private suspend fun uploadImageToStorage(imageUri: Uri, userId: String): String? {
        return try {
            val storageRef = storage.reference
            val imageFileName = "profile_pictures/$userId.jpg"
            val imageRef = storageRef.child(imageFileName)

            val uploadTask = imageRef.putFile(imageUri).await()
            uploadTask.storage.downloadUrl.await().toString()
        } catch (e: Exception) {
            // Handle the exception
            Log.e("ProfileViewModel", "Error uploading image: ${e.message}")
            null
        }
    }

    private suspend fun deleteOldProfilePicture(oldUrl: String) {
        try {
            val oldImageRef = storage.getReferenceFromUrl(oldUrl)
            oldImageRef.delete().await()
            Log.d("ProfileViewModel", "Old profile picture deleted successfully")
        } catch (e: Exception) {
            // Handle the exception
            Log.e("ProfileViewModel", "Error deleting old profile picture: ${e.message}")
        }
    }
}

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val userProfile: UserProfile?) : ProfileState()
    data class Error(val message: String) : ProfileState()
}