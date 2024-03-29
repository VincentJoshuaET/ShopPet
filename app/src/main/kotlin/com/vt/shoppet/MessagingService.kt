package com.vt.shoppet

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vt.shoppet.model.Chat
import com.vt.shoppet.repo.AuthRepo
import com.vt.shoppet.repo.ChatCache
import com.vt.shoppet.repo.FirestoreRepo
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var auth: AuthRepo

    @Inject
    lateinit var firestore: FirestoreRepo

    @Inject
    lateinit var chat: ChatCache

    override fun onNewToken(token: String) {
        if (auth.isLoggedIn()) firestore.addToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val id = message.data["CHAT_ID"] ?: return
        val senderIndex = message.data["SENDER_INDEX"] ?: return
        val receiverIndex = message.data["RECIPIENT_INDEX"] ?: return
        val senderUsername = message.data["SENDER_USERNAME"] ?: return

        chat.setValue(Chat.Event(id, senderIndex, receiverIndex, senderUsername))
    }

}