package com.vt.shoppet.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeler
import com.vt.shoppet.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LabelerRepo @Inject constructor(private val labeler: ImageLabeler) {

    fun process(image: InputImage): LiveData<Result<List<ImageLabel>>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val task = labeler.process(image).await()
                emit(Result.Success(task))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

}