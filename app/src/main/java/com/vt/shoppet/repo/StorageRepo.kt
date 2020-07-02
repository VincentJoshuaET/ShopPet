package com.vt.shoppet.repo

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.vt.shoppet.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepo @Inject constructor(
    private val auth: AuthRepo,
    private val storage: FirebaseStorage
) {

    fun getUserPhoto(id: String) = storage.reference.child("users/$id.jpg")

    fun getPetPhoto(image: String) = storage.reference.child("pets/$image.jpg")

    fun getMessagePhoto(image: String) = storage.reference.child("chats/$image.jpg")

    fun uploadPetPhoto(image: String, uri: Uri): LiveData<Result<UploadTask.TaskSnapshot>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val task = storage.reference.child("pets/$image.jpg").putFile(uri).await()
                emit(Result.Success(task))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun uploadUserPhoto(uri: Uri, id: String): LiveData<Result<UploadTask.TaskSnapshot>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val task = storage.reference.child("users/$id.jpg").putFile(uri).await()
                emit(Result.Success(task))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun uploadMessagePhoto(image: String, uri: Uri): LiveData<Result<UploadTask.TaskSnapshot>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val task = storage.reference.child("chats/$image.jpg").putFile(uri).await()
                emit(Result.Success(task))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun removePetPhoto(image: String): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val task = storage.reference.child("pets/$image.jpg").delete().await()
                emit(Result.Success(task))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun removeUserPhoto(): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val task = storage.reference.child("users/${auth.uid()}.jpg").delete().await()
                emit(Result.Success(task))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

}