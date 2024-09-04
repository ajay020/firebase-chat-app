package com.example.chatapp.data.model

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val isOnline:Boolean = false,
    val lastSeen: Long? = null,
    var profilePictureUrl: String? = null,
    var fcmToken: String? = null
)
