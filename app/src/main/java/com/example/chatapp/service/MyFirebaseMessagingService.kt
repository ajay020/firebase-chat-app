package com.example.chatapp.service

import android.util.Log
import com.example.chatapp.data.repository.AuthRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.chatapp.MainActivity
import com.example.chatapp.R

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onNewToken(token: String) {
        // Send this token to your server to store it for later use
        val user = authRepository.getCurrentUser()
        user?.let {
            authRepository.saveFcmToken(it.uid, token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("MyFirebaseMessagingService", "onMessageReceived: called")

        // Display the notification or update the UI
        remoteMessage.data.isNotEmpty().let {
            // Handle data payload
            val title = remoteMessage.data["title"]
            val body = remoteMessage.data["body"]

            Log.d("MyFirebaseMessagingService", "onMessageReceived: $title $body")

            // Show notification
            title?.let { titleText ->
                body?.let { bodyText ->
                    showNotification(titleText, bodyText)
                }
            }
        }
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "chat_app_notifications"
        val channelName = "Chat Notifications"

        // Create an intent that opens the app when the notification is clicked
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent
            .getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Create a notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Notifications for new messages in chat app"
            }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_google) // Your app icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Show the notification
        with(NotificationManagerCompat.from(this)) {
            notify(0, builder.build())
        }
    }
}