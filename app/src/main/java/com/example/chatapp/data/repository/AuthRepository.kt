package com.example.chatapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signUpWithEmailPassword(
        displayName: String,
        email: String,
        password: String
    ): Flow<Result<FirebaseUser?>>

    suspend fun signInWithEmailPassword(
        email: String,
        password: String
    ): Flow<Result<FirebaseUser?>>

    fun signOut()
    fun getCurrentUser(): FirebaseUser?
    fun updateUserStatus(isOnline: Boolean)
}

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun signUpWithEmailPassword(
        displayName: String,
        email: String,
        password: String
    ): Flow<Result<FirebaseUser?>> = flow {
        try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            emit(Result.success(user))

        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun signInWithEmailPassword(
        email: String,
        password: String
    ): Flow<Result<FirebaseUser?>> = flow {
        try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            emit(Result.success(result.user))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun updateUserStatus(isOnline: Boolean) {
        val userId = firebaseAuth.currentUser?.uid
        val userRef = userId?.let { firestore.collection("users").document(it) }

        userRef?.update(
            mapOf(
                "isOnline" to isOnline,
                "lastSeen" to if (isOnline) null else System.currentTimeMillis()
            )
        )
    }

    override fun signOut() {
        updateUserStatus(false)
        firebaseAuth.signOut()
    }

    override fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser
}
