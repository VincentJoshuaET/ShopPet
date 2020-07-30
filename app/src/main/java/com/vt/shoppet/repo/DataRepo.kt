package com.vt.shoppet.repo

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.Flow

interface DataRepo {
    val currentUserFlow: Flow<DocumentSnapshot>
    val petsFlow: Flow<QuerySnapshot>
    val starredPetsFlow: Flow<QuerySnapshot>
    val ownPetsFlow: Flow<QuerySnapshot>
    val chatsFlow: Flow<QuerySnapshot>
}