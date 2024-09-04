package com.example.chatapp.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signUpWithEmailPassword(
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
    fun saveFcmToken(userId: String)
    fun handleTokenRefresh()
    fun saveFcmToken(userId: String, token: String)
}

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val firebaseMessaging: FirebaseMessaging
) : AuthRepository {

    override suspend fun signUpWithEmailPassword(
        email: String,
        password: String
    ): Flow<Result<FirebaseUser?>> = flow {
        try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user

            user?.let {
                val userProfile = hashMapOf(
                    "uid" to user.uid,
                    "displayName" to generateDisplayName(email),
                    "email" to email,
                    "isOnline" to true,
                    "lastSeen" to System.currentTimeMillis()
                )
                firestore.collection("users").document(user.uid).set(userProfile).await()
                saveFcmToken(it.uid)
            }
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
            val user = result.user
            user?.let {
                saveFcmToken(it.uid)
            }
            Log.d("AuthRepositoryImpl", "signInWithEmailPassword: $user")
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

    override fun saveFcmToken(userId: String) {
        firebaseMessaging.token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@addOnCompleteListener
            }

            if (task.isSuccessful) {
                val token = task.result
                firestore.collection("users").document(userId).update("fcmToken", token)
            }
        }
    }

    override fun saveFcmToken(userId: String, token: String) {
        firestore.collection("users").document(userId).update("fcmToken", token)
    }

    override fun handleTokenRefresh() {
        firebaseMessaging.token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                val userId = firebaseAuth.currentUser?.uid
                userId?.let {
                    firestore.collection("users").document(it).update("fcmToken", token)
                }
            }
        }
    }

    override fun signOut() {
        updateUserStatus(false)
        firebaseAuth.signOut()
    }

    override fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    private fun generateDisplayName(email: String): String {
        email.split("@").let {
            return it[0]
        }
    }
}
