package com.vt.shoppet.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.vt.shoppet.firebase.AuthRepo
import com.vt.shoppet.firebase.FirestoreRepo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var auth: AuthRepo

    @Inject
    lateinit var firestore: FirestoreRepo

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onNewToken(token: String) {
        if (auth.isLoggedIn()) scope.launch {
            firestore.addToken(token)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

}