package com.example.chatapp.di

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
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()

    }

    @Provides
    @Singleton
    fun provideAuthRepository(firebaseAuth: FirebaseAuth): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth, provideFirestore())
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