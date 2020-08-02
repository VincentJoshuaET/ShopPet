package com.vt.shoppet.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.vt.shoppet.model.Chat
import com.vt.shoppet.model.Message
import com.vt.shoppet.model.Pet
import com.vt.shoppet.model.User
import com.vt.shoppet.repo.FirestoreRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirestoreViewModel @ViewModelInject constructor(private val firestore: FirestoreRepo) :
    ViewModel() {

    fun checkUsername(username: String): LiveData<Result<QuerySnapshot>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                firestore.checkUsername(username)
            }
            emit(result)
        }

    fun addUser(user: User): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                firestore.addUser(user)
            }
            emit(result)
        }

    fun getUserSnapshot(uid: String): LiveData<Result<DocumentSnapshot>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                firestore.getUserSnapshot(uid)
            }
            emit(result)
        }

    fun addToken(token: String) = firestore.addToken(token)

    fun removeToken(token: String): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                firestore.removeToken(token)
            }
            emit(result)
        }

    fun getReport(uid: String, currentUid: String): LiveData<Result<DocumentSnapshot>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                firestore.getReport(uid, currentUid)
            }
            emit(result)
        }

    fun addReport(uid: String): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                firestore.addReport(uid)
            }
            emit(result)
        }

    fun reportUser(uid: String, currentUid: String): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                firestore.reportUser(uid, currentUid)
            }
            emit(result)
        }

    fun updateUser(user: User): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                firestore.updateUser(user)
            }
            emit(result)
        }

    fun removeUserPhoto(): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                firestore.removeUserPhoto()
            }
            emit(result)
        }

    fun addPet(pet: Pet): LiveData<Result<DocumentReference>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                firestore.addPet(pet)
            }
            emit(result)
        }

    fun updatePet(pet: Pet): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                firestore.updatePet(pet)
            }
            emit(result)
        }

    fun checkStarredPet(id: String): LiveData<Result<DocumentSnapshot>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                firestore.checkStarredPet(id)
            }
            emit(result)
        }

    fun starPet(pet: Pet): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                firestore.starPet(pet)
            }
            emit(result)
        }

    fun unstarPet(id: String): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                firestore.unstarPet(id)
            }
            emit(result)
        }

    fun markSoldPet(id: String): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                firestore.markSoldPet(id)
            }
            emit(result)
        }

    fun removePet(id: String): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                firestore.removePet(id)
            }
            emit(result)
        }


    fun getChat(id: String): LiveData<Result<DocumentSnapshot>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                firestore.getChat(id)
            }
            emit(result)
        }

    fun checkChat(uid: String, currentUid: String): LiveData<Result<QuerySnapshot>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                firestore.checkChat(uid, currentUid)
            }
            emit(result)
        }

    fun createChat(chat: Chat): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                firestore.createChat(chat)
            }
            emit(result)
        }

    fun updateChat(chat: Chat): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                firestore.updateChat(chat)
            }
            emit(result)
        }

    fun markChatAsRead(chat: Chat, senderIndex: Int) =
        viewModelScope.launch(Dispatchers.IO) {
            firestore.updateChat(chat.copy(read = chat.read.also { it[senderIndex] = true }))
        }

    fun getMessages(id: String) = firestore.getMessages(id)

    fun sendMessage(chat: Chat, message: Message): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                firestore.sendMessage(chat, message)
            }
            emit(result)
        }
}