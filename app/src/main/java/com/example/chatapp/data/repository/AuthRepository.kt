package com.example.chatapp.data.repository

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signUpWithEmailPassword(
        displayName:String,
        email: String,
        password: String
    ): Flow<Result<FirebaseUser?>>

    suspend fun signInWithEmailPassword(
        email: String,
        password: String
    ): Flow<Result<FirebaseUser?>>

    fun signOut()
    fun getCurrentUser(): FirebaseUser?
}
