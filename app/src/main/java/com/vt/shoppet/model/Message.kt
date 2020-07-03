package com.vt.shoppet.model

import com.google.firebase.Timestamp

data class Message(

    var message: String = "",
    var chatid: String = "",
    var senderid: String = "",
    var recipientid: String = "",
    var senderusername: String = "",
    var recipientusername: String = "",
    var image: String? = null,
    var date: Timestamp = Timestamp.now()

)