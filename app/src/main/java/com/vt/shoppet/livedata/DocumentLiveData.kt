package com.vt.shoppet.livedata

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.*
import com.vt.shoppet.model.Result

class DocumentLiveData(private val reference: DocumentReference) :
    LiveData<Result<DocumentSnapshot>>(),
    EventListener<DocumentSnapshot> {

    private var registration: ListenerRegistration? = null

    override fun onEvent(snapshot: DocumentSnapshot?, exception: FirebaseFirestoreException?) {
        value = when {
            snapshot != null -> Result.Success(snapshot)
            exception != null -> Result.Failure(exception)
            else -> Result.Loading()
        }
    }

    override fun onActive() {
        super.onActive()
        registration = reference.addSnapshotListener(this)
    }

    override fun onInactive() {
        super.onInactive()
        registration?.remove()
        registration = null
    }

}