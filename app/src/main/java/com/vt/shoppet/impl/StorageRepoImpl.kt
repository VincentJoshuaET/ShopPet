package com.vt.shoppet.impl

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.vt.shoppet.firebase.StorageRepo
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepoImpl @Inject constructor(private val storage: FirebaseStorage) : StorageRepo {

    override fun getUserPhoto(id: String) = storage.reference.child("users/$id.jpg")

    override fun getPetPhoto(id: String) = storage.reference.child("pets/$id.jpg")

    override fun getMessagePhoto(id: String) = storage.reference.child("chats/$id.jpg")

    override suspend fun uploadPetPhoto(id: String, uri: Uri): UploadTask.TaskSnapshot =
        storage.reference.child("pets/$id.jpg").putFile(uri).await()

    override suspend fun uploadUserPhoto(id: String, uri: Uri): UploadTask.TaskSnapshot =
        storage.reference.child("users/$id.jpg").putFile(uri).await()

    override suspend fun uploadMessagePhoto(id: String, uri: Uri): UploadTask.TaskSnapshot =
        storage.reference.child("chats/$id.jpg").putFile(uri).await()

    override suspend fun removePetPhoto(id: String): Void? =
        storage.reference.child("pets/$id.jpg").delete().await()

    override suspend fun removeUserPhoto(id: String): Void? =
        storage.reference.child("users/$id.jpg").delete().await()

}