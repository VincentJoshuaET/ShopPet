package com.vt.shoppet.impl

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.vt.shoppet.repo.DataRepo
import com.vt.shoppet.repo.FirestoreRepo
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataRepoImpl @Inject constructor(private val firestore: FirestoreRepo) : DataRepo {
    override val currentUserFlow: Flow<DocumentSnapshot> =
        callbackFlow {
            val registration =
                firestore.getUserReference(firestore.uid)
                    .addSnapshotListener { document, exception ->
                        if (exception != null) {
                            val message = exception.localizedMessage ?: "Error"
                            cancel(message, exception)
                        }
                        if (document != null) offer(document)
                    }
            awaitClose {
                registration.remove()
            }
        }
    override val petsFlow: Flow<QuerySnapshot> =
        callbackFlow {
            val registration = firestore.getPets().addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    val message = exception.localizedMessage ?: "Error"
                    cancel(message, exception)
                }
                if (snapshots != null) offer(snapshots)
            }
            awaitClose {
                registration.remove()
            }
        }
    override val starredPetsFlow: Flow<QuerySnapshot> =
        callbackFlow {
            val registration =
                firestore.getStarredPets().addSnapshotListener { snapshots, exception ->
                    if (exception != null) {
                        val message = exception.localizedMessage ?: "Error"
                        cancel(message, exception)
                    }
                    if (snapshots != null) offer(snapshots)
                }
            awaitClose {
                registration.remove()
            }
        }
    override val ownPetsFlow: Flow<QuerySnapshot> =
        callbackFlow {
            val registration = firestore.getOwnPets().addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    val message = exception.localizedMessage ?: "Error"
                    cancel(message, exception)
                }
                if (snapshots != null) offer(snapshots)
            }
            awaitClose {
                registration.remove()
            }
        }
    override val chatsFlow: Flow<QuerySnapshot> =
        callbackFlow {
            val registration = firestore.getChats().addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    val message = exception.localizedMessage ?: "Error"
                    cancel(message, exception)
                }
                if (snapshots != null) offer(snapshots)
            }
            awaitClose {
                registration.remove()
            }
        }
}