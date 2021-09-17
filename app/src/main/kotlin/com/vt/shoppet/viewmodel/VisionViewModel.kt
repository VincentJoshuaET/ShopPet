package com.vt.shoppet.viewmodel

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.vt.shoppet.repo.VisionRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class VisionViewModel @Inject constructor(private val vision: VisionRepo) : ViewModel() {

    fun process(image: InputImage): LiveData<Result<List<ImageLabel>>> =
        liveData {
            val result = runCatching {
                vision.process(image)
            }
            emit(result)
        }

    fun convertImage(context: Context, uri: Uri): LiveData<Result<InputImage>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                InputImage.fromFilePath(context, uri)
            }
            emit(result)
        }

    @OptIn(ExperimentalGetImage::class)
    fun convertImage(proxy: ImageProxy): LiveData<Result<InputImage>> =
        liveData(Dispatchers.IO) {
            val image = proxy.image ?: return@liveData
            val result = runCatching {
                InputImage.fromMediaImage(image, proxy.imageInfo.rotationDegrees)
            }
            emit(result)
        }

}