package com.example.chatapp.data.repository

import android.util.Log
import com.example.chatapp.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface ProfileRepository {
    suspend fun getUserProfile(): Result<UserProfile?>
    suspend fun saveUserProfile(userProfile: UserProfile): Result<Unit>
}

class ProfileRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : ProfileRepository {

    private val userCollection = firestore.collection("users")

    override suspend fun getUserProfile(): Result<UserProfile?> {
        return try {
            val userId = firebaseAuth.currentUser?.uid
                ?: return Result.failure(Exception("User not authenticated"))
            val documentSnapshot = userCollection.document(userId).get().await()
            if (documentSnapshot.exists()) {
                val user = documentSnapshot.toObject(UserProfile::class.java)
                Log.d("ProfileRepositoryImpl", "getUserProfile: $user.toString()")
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveUserProfile(userProfile: UserProfile): Result<Unit> {
        return try {
            val userId = userProfile.uid
            val userDocRef = userCollection.document(userId)

            // Get the document to check if it exists
            val documentSnapshot = userDocRef.get().await()

            if (documentSnapshot.exists()) {
                // Document exists, so update it
                val updates = mutableMapOf<String, Any?>()
                updates["uid"] = userId
                userProfile.displayName.let {
                    updates["displayName"] = it
                }
                userProfile.profilePictureUrl?.let {
                    updates["profilePictureUrl"] = it
                }

                userCollection.document(userId).update(updates).await()
            } else {
                // Document doesn't exist, so create it
                userDocRef.set(userProfile).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}