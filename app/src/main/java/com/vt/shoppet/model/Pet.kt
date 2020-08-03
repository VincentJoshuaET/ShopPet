package com.vt.shoppet.model

import com.google.firebase.Timestamp

data class Pet(
    var name: String = "",
    var uid: String = "",
    var username: String = "",
    var image: String = "",
    var type: String = "",
    var price: Int = 0,
    var sex: String = "",
    var dateOfBirth: Timestamp = Timestamp.now(),
    var breed: String = "",
    var vaccineStatus: String = "",
    var medicalRecords: String = "",
    var description: String = "",
    var sold: Boolean = false,
    var id: String = "",
    val date: Timestamp = Timestamp.now()
)