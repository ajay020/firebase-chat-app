package com.example.chatapp.di

import android.util.Log
import com.example.chatapp.data.repository.AuthRepository
import com.example.chatapp.data.repository.AuthRepositoryImpl
import com.example.chatapp.data.repository.ChatRepository
import com.example.chatapp.data.repository.ChatRepositoryImpl
import com.example.chatapp.data.repository.ProfileRepository
import com.example.chatapp.data.repository.ProfileRepositoryImpl
import com.example.chatapp.data.repository.UserRepository
import com.example.chatapp.data.repository.UserRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatModule {
    private const val LOCALHOST_IP = "192.168.172.37" // Replace with your laptop's IP
    private const val FIRESTORE_PORT = 8080
    private const val AUTH_PORT = 9099
    private const val STORAGE_PORT = 9199

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
//        return FirebaseAuth.getInstance()
        val auth = FirebaseAuth.getInstance()
        // Connect to the Authentication emulator
        auth.useEmulator(LOCALHOST_IP, AUTH_PORT)
        return auth
    }

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()
        try {
            firestore.useEmulator(LOCALHOST_IP, FIRESTORE_PORT)
        } catch (e: IllegalStateException){
            Log.e("FirestoreModule", "$e")
        }
        return firestore
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        val storage = FirebaseStorage.getInstance()
        // Connect to the Storage emulator
        storage.useEmulator(LOCALHOST_IP, STORAGE_PORT)
        return storage
    }

    // Repository Providers
    @Provides
    @Singleton
    fun provideAuthRepository(firebaseAuth: FirebaseAuth): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth, provideFirestore(), provideFirebaseMessaging())
    }
    @Provides
    @Singleton
    fun provideUserRepository(firestore: FirebaseFirestore): UserRepository {
        return UserRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideChatRepository(firestore: FirebaseFirestore): ChatRepository {
        return ChatRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideProfileRepository(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): ProfileRepository {
        return ProfileRepositoryImpl(firestore, firebaseAuth)
    }
}