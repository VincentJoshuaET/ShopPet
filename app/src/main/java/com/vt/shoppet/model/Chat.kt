package com.vt.shoppet.model

import com.google.firebase.Timestamp

data class Chat(
    var id: String = "",
    var message: String? = null,
    var uid: List<String> = emptyList(),
    var username: List<String> = emptyList(),
    var read: List<Boolean> = emptyList(),
    var empty: Boolean = false,
    var date: Timestamp = Timestamp.now()
)