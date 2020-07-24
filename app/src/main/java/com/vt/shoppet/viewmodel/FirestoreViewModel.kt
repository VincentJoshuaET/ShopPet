package com.vt.shoppet.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.vt.shoppet.model.*
import com.vt.shoppet.repo.FirestoreRepo
import kotlinx.coroutines.Dispatchers

class FirestoreViewModel @ViewModelInject constructor(private val firestore: FirestoreRepo) :
    ViewModel() {

    fun checkUsername(username: String): LiveData<Result<QuerySnapshot>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(firestore.checkUsername(username)))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun addUser(user: User): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(firestore.addUser(user)))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun getUserSnapshot(uid: String): LiveData<Result<DocumentSnapshot>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(firestore.getUserSnapshot(uid)))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun addToken(token: String) = firestore.addToken(token)

    fun removeToken(token: String) = firestore.removeToken(token)

    fun getReport(uid: String, currentUid: String): LiveData<Result<DocumentSnapshot>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(firestore.getReport(uid, currentUid)))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun addReport(uid: String): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(firestore.addReport(uid)))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun reportUser(uid: String, currentUid: String): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(firestore.reportUser(uid, currentUid)))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun updateUser(user: User): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(firestore.updateUser(user)))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun removeUserPhoto(): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(firestore.removeUserPhoto()))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun addPet(pet: Pet): LiveData<Result<DocumentReference>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(firestore.addPet(pet)))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun updatePet(pet: Pet): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(firestore.updatePet(pet)))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun checkStarredPet(id: String): LiveData<Result<DocumentSnapshot>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(firestore.checkStarredPet(id)))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun starPet(pet: Pet): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(firestore.starPet(pet)))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun unstarPet(id: String): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(firestore.unstarPet(id)))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun markSoldPet(id: String): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(firestore.markSoldPet(id)))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun removePet(id: String): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(firestore.removePet(id)))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun checkChat(uid: String, currentUid: String): LiveData<Result<QuerySnapshot>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(firestore.checkChat(uid, currentUid)))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun createChat(chat: Chat): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(firestore.createChat(chat)))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun updateChat(chat: Chat): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(firestore.updateChat(chat)))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun getMessages(id: String) = firestore.getMessages(id)

    fun sendMessage(chat: Chat, message: Message): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(firestore.sendMessage(chat, message)))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }
}