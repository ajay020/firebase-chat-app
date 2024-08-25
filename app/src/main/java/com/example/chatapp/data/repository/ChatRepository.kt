package com.example.chatapp.data.repository

import android.util.Log
import com.example.chatapp.data.model.Chat
import com.example.chatapp.data.model.Message
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface ChatRepository {
    suspend fun saveChatAndMessage(chatId: String, chat: Chat, message: Message)
    suspend fun getMessages(chatId: String): Flow<Result<List<Message>?>>
}

class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChatRepository {

    override suspend fun saveChatAndMessage(chatId: String, chat: Chat, message: Message) {
        // Get references to the chat and the new message document
        val chatRoomRef = firestore.collection("chats").document(chatId)
        val messageRef = chatRoomRef.collection("messages").document()

        // Start a batch write
        firestore.runBatch { batch ->
            // Update or create the chat document
            batch.set(chatRoomRef, chat)

            // Add the new message document to the messages sub-collection
            batch.set(messageRef, message)
        }.await() // await() if you're using coroutines

        Log.d("Firestore", "Chat and message saved successfully")
    }


    override suspend fun getMessages(chatId: String): Flow<Result<List<Message>?>> {
        Log.d("ChatRepository", "Fetching messages for chatId: $chatId")

        return callbackFlow {
            val messagesRef = firestore.collection("chats").document(chatId)
                .collection("messages")
                .orderBy("timestamp")


            val listenerRegistration = messagesRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // If there's an error, emit a failure result and close the flow
                    trySend(Result.failure(error))
                    close(error)
                    Log.e("ChatRepository", "Error fetching messages: ${error.message}")
                    return@addSnapshotListener
                }

                Log.d("ChatRepository", "Snapshot data: ${snapshot?.documents}")

                if (snapshot != null && !snapshot.isEmpty) {
                    val messages = snapshot.documents.mapNotNull { document ->
                        document.toObject(Message::class.java)
                    }
                    Log.d("ChatRepository", "Messages fetched successfully: $messages")

                    // Emit the list of messages
                    trySend(Result.success(messages))
                } else {
                    Log.d("ChatRepository", "No messages found")
                    // If no document exists, emit null
                    trySend(Result.success(null))
                }
            }

            awaitClose {
                listenerRegistration.remove() // Remove the listener when the flow is closed
            }
        }
    }
}
