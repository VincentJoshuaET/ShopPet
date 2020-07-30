package com.vt.shoppet.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.vt.shoppet.repo.LabelerRepo
import kotlinx.coroutines.Dispatchers

class LabelerViewModel @ViewModelInject constructor(
    private val labeler: LabelerRepo
) : ViewModel() {

    fun process(image: InputImage): LiveData<Result<List<ImageLabel>>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                labeler.process(image)
            }
            emit(result)
        }

}