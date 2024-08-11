package com.example.chatapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override suspend fun signUpWithEmailPassword(
        email: String,
        password: String
    ): Flow<Result<FirebaseUser?>> = flow {
        try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            emit(Result.success(result.user))
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

    override fun signOut() {
        firebaseAuth.signOut()
    }

    override fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser
}
