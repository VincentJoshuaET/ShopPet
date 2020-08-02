package com.vt.shoppet.viewmodel

import android.content.Context
import android.net.Uri
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import com.vt.shoppet.repo.LabelerRepo
import kotlinx.coroutines.Dispatchers

class LabelerViewModel @ViewModelInject constructor(
    private val labeler: LabelerRepo
) : ViewModel() {

    fun process(image: FirebaseVisionImage): LiveData<Result<List<FirebaseVisionImageLabel>>> =
        liveData {
            val result = runCatching {
                labeler.process(image)
            }
            emit(result)
        }

    fun convertImage(context: Context, uri: Uri): LiveData<Result<FirebaseVisionImage>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                FirebaseVisionImage.fromFilePath(context, uri)
            }
            emit(result)
        }

    @ExperimentalGetImage
    fun convertImage(proxy: ImageProxy): LiveData<Result<FirebaseVisionImage>> =
        liveData(Dispatchers.IO) {
            val image = proxy.image ?: return@liveData
            val degrees = when (proxy.imageInfo.rotationDegrees) {
                0 -> FirebaseVisionImageMetadata.ROTATION_0
                90 -> FirebaseVisionImageMetadata.ROTATION_90
                180 -> FirebaseVisionImageMetadata.ROTATION_180
                270 -> FirebaseVisionImageMetadata.ROTATION_270
                else -> throw Exception("Rotation must be 0, 90, 180, or 270.")
            }
            val result = runCatching {
                FirebaseVisionImage.fromMediaImage(image, degrees)
            }
            emit(result)
        }

}