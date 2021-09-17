package com.vt.shoppet.repo

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@ActivityRetainedScoped
class StorageRepo @Inject constructor(private val storage: FirebaseStorage) {

    fun getUserPhoto(id: String) = storage.reference.child("users/$id.jpg")

    fun getPetPhoto(id: String) = storage.reference.child("pets/$id.jpg")

    fun getMessagePhoto(id: String) = storage.reference.child("chats/$id.jpg")

    suspend fun uploadPetPhoto(id: String, uri: Uri): UploadTask.TaskSnapshot =
        storage.reference.child("pets/$id.jpg").putFile(uri).await()

    suspend fun uploadUserPhoto(id: String, uri: Uri): UploadTask.TaskSnapshot =
        storage.reference.child("users/$id.jpg").putFile(uri).await()

    suspend fun uploadMessagePhoto(id: String, uri: Uri): UploadTask.TaskSnapshot =
        storage.reference.child("chats/$id.jpg").putFile(uri).await()

    suspend fun removePetPhoto(id: String): Void? =
        storage.reference.child("pets/$id.jpg").delete().await()

    suspend fun removeUserPhoto(id: String): Void? =
        storage.reference.child("users/$id.jpg").delete().await()

}