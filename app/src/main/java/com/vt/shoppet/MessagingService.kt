package com.vt.shoppet

import android.app.NotificationManager
import android.content.Intent
import androidx.core.os.bundleOf
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vt.shoppet.repo.AuthRepo
import com.vt.shoppet.repo.FirestoreRepo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class MessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var auth: AuthRepo

    @Inject
    lateinit var firestore: FirestoreRepo

    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onNewToken(token: String) {
        if (auth.isLoggedIn()) firestore.addToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val id = message.data["CHAT_ID"] ?: return
        val senderIndex = message.data["SENDER_INDEX"] ?: return
        val receiverIndex = message.data["RECIPIENT_INDEX"] ?: return
        val senderUsername = message.data["SENDER_USERNAME"] ?: return

        val bundle = bundleOf(
            "CHAT_ID" to id,
            "SENDER_INDEX" to senderIndex,
            "RECIPIENT_INDEX" to receiverIndex,
            "SENDER_USERNAME" to senderUsername
        )

        val intent = Intent("ACTION_CHAT").apply {
            putExtras(bundle)
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

}