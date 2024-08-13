package com.example.chatapp.data.repository

import com.example.chatapp.data.model.ChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChatRepository {

    override fun sendMessage(chatMessage: ChatMessage) {
        firestore.collection("chats")
            .add(chatMessage)
    }

    override fun getMessages(): Flow<List<ChatMessage>> = callbackFlow {
        val listener = firestore.collection("chats")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                val messages = snapshot?.toObjects(ChatMessage::class.java)
                trySend(messages ?: emptyList())
            }

        awaitClose { listener.remove() }
    }
}
