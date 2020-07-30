package com.vt.shoppet.viewmodel

import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.google.firebase.storage.UploadTask
import com.vt.shoppet.repo.StorageRepo
import kotlinx.coroutines.Dispatchers

class StorageViewModel @ViewModelInject constructor(
    private val storage: StorageRepo
) : ViewModel() {

    fun getUserPhoto(id: String) = storage.getUserPhoto(id)

    fun getPetPhoto(id: String) = storage.getPetPhoto(id)

    fun getMessagePhoto(id: String) = storage.getMessagePhoto(id)

    fun uploadPetPhoto(id: String, uri: Uri): LiveData<Result<UploadTask.TaskSnapshot>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                storage.uploadPetPhoto(id, uri)
            }
            emit(result)
        }

    fun uploadUserPhoto(id: String, uri: Uri): LiveData<Result<UploadTask.TaskSnapshot>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                storage.uploadUserPhoto(id, uri)
            }
            emit(result)
        }

    fun uploadMessagePhoto(id: String, uri: Uri): LiveData<Result<UploadTask.TaskSnapshot>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                storage.uploadMessagePhoto(id, uri)
            }
            emit(result)
        }

    fun removePetPhoto(id: String): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                storage.removePetPhoto(id)
            }
            emit(result)
        }

    fun removeUserPhoto(id: String): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                storage.removeUserPhoto(id)
            }
            emit(result)
        }

}