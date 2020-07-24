package com.vt.shoppet.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.vt.shoppet.repo.AuthRepo
import com.vt.shoppet.repo.FirestoreRepo
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var auth: AuthRepo

    @Inject
    lateinit var firestore: FirestoreRepo

    override fun onNewToken(token: String) {
        if (auth.isLoggedIn()) firestore.addToken(token)
    }

}