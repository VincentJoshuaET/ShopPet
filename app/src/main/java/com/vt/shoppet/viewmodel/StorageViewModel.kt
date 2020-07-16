package com.vt.shoppet.viewmodel

import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.google.firebase.storage.UploadTask
import com.vt.shoppet.firebase.StorageRepo
import com.vt.shoppet.model.Result
import kotlinx.coroutines.Dispatchers

class StorageViewModel @ViewModelInject constructor(
    private val storage: StorageRepo
) : ViewModel() {

    fun getUserPhoto(id: String) = storage.getUserPhoto(id)

    fun getPetPhoto(id: String) = storage.getPetPhoto(id)

    fun getMessagePhoto(id: String) = storage.getMessagePhoto(id)

    fun uploadPetPhoto(id: String, uri: Uri): LiveData<Result<UploadTask.TaskSnapshot>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(storage.uploadPetPhoto(id, uri)))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun uploadUserPhoto(id: String, uri: Uri): LiveData<Result<UploadTask.TaskSnapshot>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(storage.uploadUserPhoto(id, uri)))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun uploadMessagePhoto(id: String, uri: Uri): LiveData<Result<UploadTask.TaskSnapshot>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(storage.uploadMessagePhoto(id, uri)))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun removePetPhoto(id: String): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(storage.removePetPhoto(id)))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun removeUserPhoto(id: String): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(storage.removeUserPhoto(id)))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

}