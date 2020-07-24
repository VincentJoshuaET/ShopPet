package com.vt.shoppet.repo

import android.net.Uri
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask

interface StorageRepo {
    fun getUserPhoto(id: String): StorageReference

    fun getPetPhoto(id: String): StorageReference

    fun getMessagePhoto(id: String): StorageReference

    suspend fun uploadPetPhoto(id: String, uri: Uri): UploadTask.TaskSnapshot

    suspend fun uploadUserPhoto(id: String, uri: Uri): UploadTask.TaskSnapshot

    suspend fun uploadMessagePhoto(id: String, uri: Uri): UploadTask.TaskSnapshot

    suspend fun removePetPhoto(id: String): Void?

    suspend fun removeUserPhoto(id: String): Void?
}