package com.example.chatapp.data.repository

import com.example.chatapp.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface UserRepository {
    suspend fun getAllUsers(): Result<List<UserProfile>>
}

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {
    private val userCollection = firestore.collection("users")

    override suspend fun getAllUsers(): Result<List<UserProfile>> {
        return try {
            val snapshot = userCollection.get().await()
            val users = snapshot.documents.mapNotNull { document ->
                document.toObject(UserProfile::class.java)
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}