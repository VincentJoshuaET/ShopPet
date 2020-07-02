package com.vt.shoppet.livedata

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.*
import com.vt.shoppet.model.Result

class QueryLiveData(private val query: Query) :
    LiveData<Result<QuerySnapshot>>(),
    EventListener<QuerySnapshot> {

    private var registration: ListenerRegistration? = null

    override fun onEvent(snapshot: QuerySnapshot?, exception: FirebaseFirestoreException?) {
        value = when {
            snapshot != null -> Result.Success(snapshot)
            exception != null -> Result.Failure(exception)
            else -> Result.Loading()
        }
    }

    override fun onActive() {
        super.onActive()
        registration = query.addSnapshotListener(this)
    }

    override fun onInactive() {
        super.onInactive()
        registration?.remove()
        registration = null
    }

}