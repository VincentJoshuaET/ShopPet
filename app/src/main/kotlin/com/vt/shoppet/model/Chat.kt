package com.vt.shoppet.model

import com.google.firebase.Timestamp

data class Chat(
    var id: String = "",
    var message: String? = null,
    var uid: List<String> = emptyList(),
    var username: List<String> = emptyList(),
    var read: MutableList<Boolean> = mutableListOf(),
    var empty: Boolean = false,
    var date: Timestamp = Timestamp.now()
) {
    class Event(val id: String, val senderIndex: String, val receiverIndex: String, val senderUsername: String)
}