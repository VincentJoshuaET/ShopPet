package com.vt.shoppet.firebase

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.vt.shoppet.model.Chat
import com.vt.shoppet.model.Message
import com.vt.shoppet.model.Pet
import com.vt.shoppet.model.User

interface FirestoreRepo {
    suspend fun checkUsername(username: String): QuerySnapshot

    suspend fun addUser(user: User): Void?

    fun getUserReference(uid: String): DocumentReference

    suspend fun getUserSnapshot(uid: String): DocumentSnapshot

    suspend fun addToken(token: String): Void?

    suspend fun removeToken(token: String): Void?

    suspend fun getReport(uid: String, currentUid: String): DocumentSnapshot

    suspend fun addReport(uid: String): Void?

    suspend fun reportUser(uid: String, currentUid: String): Void?

    suspend fun updateUser(user: User): Void?

    suspend fun removeUserPhoto(): Void?

    suspend fun addPet(pet: Pet): DocumentReference

    suspend fun updatePet(pet: Pet): Void?

    fun getPets(): Query

    fun getOwnPets(): Query

    fun getStarredPets(): Query

    suspend fun checkStarredPet(id: String): DocumentSnapshot

    suspend fun starPet(pet: Pet): Void?

    suspend fun unstarPet(id: String): Void?

    suspend fun markSoldPet(id: String): Void?

    suspend fun removePet(id: String): Void?

    fun getChats(): Query

    suspend fun checkChat(uid: String, currentUid: String): QuerySnapshot

    suspend fun createChat(chat: Chat): Void?

    suspend fun updateChat(chat: Chat): Void?

    fun getMessages(id: String): Query

    suspend fun sendMessage(chat: Chat, message: Message): Void?
}