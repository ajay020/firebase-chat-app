package com.example.chatapp.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class Utils {
    fun formatLastSeen(lastSeen: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - lastSeen

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes minutes ago"
            hours < 24 -> "$hours hours ago"
            days < 2 -> "Yesterday"
            days < 7 -> SimpleDateFormat(
                "EEEE",
                Locale.getDefault()
            ).format(Date(lastSeen))  // Day of the week
            else -> SimpleDateFormat(
                "dd MMM yyyy",
                Locale.getDefault()
            ).format(Date(lastSeen))  // Full date
        }
    }

}