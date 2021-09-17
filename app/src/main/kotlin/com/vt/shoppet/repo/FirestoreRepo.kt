package com.vt.shoppet.repo

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import com.vt.shoppet.model.Chat
import com.vt.shoppet.model.Message
import com.vt.shoppet.model.Pet
import com.vt.shoppet.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRepo @Inject constructor(private val auth: AuthRepo, private val firestore: FirebaseFirestore) {

    val uid: String
        get() = auth.uid() as String

    suspend fun checkUsername(username: String): QuerySnapshot =
        firestore.collection("users").whereEqualTo("username", username).get().await()

    suspend fun addUser(user: User): Void? =
        firestore.collection("users").document(user.uid).set(user).await()

    fun getUserReference(uid: String) =
        firestore.collection("users").document(uid)

    suspend fun getUserSnapshot(uid: String): DocumentSnapshot =
        firestore.collection("users").document(uid).get().await()

    fun addToken(token: String): Task<Void> =
        firestore.collection("users").document(uid)
            .update("tokens", FieldValue.arrayUnion(token))

    suspend fun removeToken(token: String): Void? =
        firestore.collection("users").document(uid)
            .update("tokens", FieldValue.arrayRemove(token)).await()

    suspend fun getReport(uid: String, currentUid: String): DocumentSnapshot =
        firestore.collection("users/$uid/reports").document(currentUid).get().await()

    suspend fun addReport(uid: String): Void? =
        firestore.collection("users").document(uid).update("reports", FieldValue.increment(1))
            .await()

    suspend fun reportUser(uid: String, currentUid: String): Void? =
        firestore.collection("users/$uid/reports").document(currentUid)
            .set(mapOf("uid" to currentUid)).await()

    suspend fun updateUser(user: User): Void? =
        firestore.collection("users").document(uid).set(user).await()

    suspend fun removeUserPhoto(): Void? =
        firestore.collection("users").document(uid).update("image", null).await()

    suspend fun addPet(pet: Pet): DocumentReference =
        firestore.collection("pets").add(pet).await()

    suspend fun updatePet(pet: Pet): Void? =
        firestore.collection("pets").document(pet.id).set(pet).await()

    fun getPets() =
        firestore.collection("pets")
            .whereEqualTo("sold", false)
            .orderBy("date", Query.Direction.DESCENDING)

    fun getOwnPets() =
        firestore.collection("pets")
            .whereEqualTo("uid", uid)
            .orderBy("date", Query.Direction.DESCENDING)

    fun getStarredPets() =
        firestore.collection("starred")
            .document(uid)
            .collection("pets")

    suspend fun checkStarredPet(id: String): DocumentSnapshot =
        firestore.collection("starred").document(uid).collection("pets").document(id).get()
            .await()

    suspend fun starPet(pet: Pet): Void? =
        firestore.collection("starred").document(uid).collection("pets").document(pet.id)
            .set(pet).await()

    suspend fun unstarPet(id: String): Void? =
        firestore.collection("starred").document(uid).collection("pets").document(id)
            .delete().await()

    suspend fun markSoldPet(id: String): Void? =
        firestore.collection("pets").document(id)
            .update(mapOf("sold" to true, "date" to Timestamp.now())).await()

    suspend fun removePet(id: String): Void? =
        firestore.collection("pets").document(id).delete().await()

    fun getChats() =
        firestore.collection("chats")
            .whereArrayContains("uid", uid)
            .whereEqualTo("empty", false)
            .orderBy("date", Query.Direction.DESCENDING)

    suspend fun getChat(id: String): DocumentSnapshot =
        firestore.collection("chats").document(id).get().await()

    suspend fun checkChat(uid: String, currentUid: String): QuerySnapshot =
        firestore.collection("chats")
            .whereIn("id", listOf("$currentUid$uid", "$uid$currentUid"))
            .get()
            .await()

    suspend fun createChat(chat: Chat): Void? =
        firestore.collection("chats").document(chat.id).set(chat).await()

    suspend fun updateChat(chat: Chat): Void? =
        firestore.collection("chats").document(chat.id).set(chat).await()

    suspend fun markChatAsRead(id: String, read: List<Boolean>): Void? =
        firestore.collection("chats").document(id).update("read", read).await()

    fun getMessages(id: String) =
        firestore.collection("chats")
            .document(id)
            .collection("messages")
            .orderBy("date", Query.Direction.ASCENDING)

    suspend fun sendMessage(chat: Chat, message: Message): Void? =
        firestore.collection("chats").document(chat.id).collection("messages").document()
            .set(message).await()

}