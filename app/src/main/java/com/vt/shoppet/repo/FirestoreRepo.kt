package com.vt.shoppet.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import com.vt.shoppet.model.Chat
import com.vt.shoppet.model.Pet
import com.vt.shoppet.model.Result
import com.vt.shoppet.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRepo @Inject constructor(
    private val auth: AuthRepo,
    private val firestore: FirebaseFirestore
) {

    fun checkUsername(username: String): LiveData<Result<QuerySnapshot>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val snapshot =
                    firestore.collection("users").whereEqualTo("username", username).get().await()
                emit(Result.Success(snapshot))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun addUser(user: User): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val task =
                    firestore.collection("users").document(user.uid).set(user).await()
                emit(Result.Success(task))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun getUser(uid: String) = firestore.collection("users").document(uid)

    fun getUserLiveData(uid: String): LiveData<Result<DocumentSnapshot>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val document = firestore.collection("users").document(uid).get().await()
                emit(Result.Success(document))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun addReport(uid: String): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val task =
                    firestore.collection("users").document(uid)
                        .update("reports", FieldValue.increment(1)).await()
                emit(Result.Success(task))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun reportUser(uid: String, currentUid: String): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val task =
                    firestore.collection("users/$uid/reports")
                        .document(currentUid).set(mapOf("uid" to currentUid)).await()
                emit(Result.Success(task))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun getReport(uid: String, currentUid: String): LiveData<Result<DocumentSnapshot>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val document =
                    firestore.collection("users/$uid/reports").document(currentUid).get().await()
                emit(Result.Success(document))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun updateUser(user: User): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val task = firestore.collection("users").document(auth.uid()).set(user).await()
                emit(Result.Success(task))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun removeUserPhoto(): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val task =
                    firestore.collection("users").document(auth.uid()).update("image", null).await()
                emit(Result.Success(task))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun addToken(token: String): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val task =
                    firestore.collection("users").document(auth.uid())
                        .update("tokens", FieldValue.arrayUnion(token)).await()
                emit(Result.Success(task))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun removeToken(token: String): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val task =
                    firestore.collection("users").document(auth.uid())
                        .update("tokens", FieldValue.arrayRemove(token)).await()
                emit(Result.Success(task))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun addPet(pet: Pet): LiveData<Result<DocumentReference>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val reference = firestore.collection("pets").add(pet).await()
                emit(Result.Success(reference))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun updatePetId(id: String): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val task = firestore.collection("pets").document(id).update("id", id).await()
                emit(Result.Success(task))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun getPets() =
        firestore.collection("pets")
            .whereEqualTo("sold", false)
            .whereEqualTo("visible", true)
            .orderBy("date", Query.Direction.DESCENDING)

    fun getOwnPets() =
        firestore.collection("pets")
            .whereEqualTo("uid", auth.uid())
            .whereEqualTo("visible", true)
            .orderBy("date", Query.Direction.DESCENDING)

    fun getStarredPets() =
        firestore.collection("starred")
            .document(auth.uid())
            .collection("pets")
            .whereEqualTo("visible", true)

    fun checkStarredPet(id: String): LiveData<Result<DocumentSnapshot>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val document =
                    firestore.collection("starred").document(auth.uid())
                        .collection("pets").document(id).get().await()
                emit(Result.Success(document))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun starPet(pet: Pet): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val task = firestore.collection("starred").document(auth.uid()).collection("pets")
                    .document(pet.id).set(pet).await()
                emit(Result.Success(task))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun unstarPet(id: String): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val task = firestore.collection("starred").document(auth.uid()).collection("pets")
                    .document(id).delete().await()
                emit(Result.Success(task))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun markPetAsSold(id: String): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val task = firestore.collection("pets").document(id)
                    .update(mapOf("sold" to true, "date" to Timestamp.now())).await()
                emit(Result.Success(task))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun removePet(id: String): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val task =
                    firestore.collection("pets").document(id).update("visible", false).await()
                emit(Result.Success(task))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun getChats() =
        firestore.collection("chats")
            .whereArrayContains("uids", auth.uid())
            .whereEqualTo("empty", false)
            .orderBy("date", Query.Direction.DESCENDING)

    fun checkChat(currentUid: String, uid: String): LiveData<Result<QuerySnapshot>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val snapshot = firestore.collection("chats")
                    .whereIn("id", listOf("$currentUid$uid", "$uid$currentUid")).get().await()
                emit(Result.Success(snapshot))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun createChat(chat: Chat): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val task = firestore.collection("chats").document(chat.id).set(chat).await()
                emit(Result.Success(task))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

}