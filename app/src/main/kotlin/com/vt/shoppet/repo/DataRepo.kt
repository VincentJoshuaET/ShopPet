package com.vt.shoppet.repo

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

@ActivityRetainedScoped
class DataRepo @Inject constructor(private val firestore: FirestoreRepo) {
    val currentUserFlow: Flow<DocumentSnapshot> =
        callbackFlow {
            val registration =
                firestore.getUserReference(firestore.uid)
                    .addSnapshotListener { document, exception ->
                        if (exception != null) {
                            val message = exception.localizedMessage ?: "Error"
                            cancel(message, exception)
                        }
                        if (document != null) trySend(document)
                    }
            awaitClose {
                registration.remove()
            }
        }
    val petsFlow: Flow<QuerySnapshot> =
        callbackFlow {
            val registration = firestore.getPets().addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    val message = exception.localizedMessage ?: "Error"
                    cancel(message, exception)
                }
                if (snapshots != null) trySend(snapshots)
            }
            awaitClose {
                registration.remove()
            }
        }
    val starredPetsFlow: Flow<QuerySnapshot> =
        callbackFlow {
            val registration =
                firestore.getStarredPets().addSnapshotListener { snapshots, exception ->
                    if (exception != null) {
                        val message = exception.localizedMessage ?: "Error"
                        cancel(message, exception)
                    }
                    if (snapshots != null) trySend(snapshots)
                }
            awaitClose {
                registration.remove()
            }
        }
    val ownPetsFlow: Flow<QuerySnapshot> =
        callbackFlow {
            val registration = firestore.getOwnPets().addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    val message = exception.localizedMessage ?: "Error"
                    cancel(message, exception)
                }
                if (snapshots != null) trySend(snapshots)
            }
            awaitClose {
                registration.remove()
            }
        }
    val chatsFlow: Flow<QuerySnapshot> =
        callbackFlow {
            val registration = firestore.getChats().addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    val message = exception.localizedMessage ?: "Error"
                    cancel(message, exception)
                }
                if (snapshots != null) trySend(snapshots)
            }
            awaitClose {
                registration.remove()
            }
        }
}