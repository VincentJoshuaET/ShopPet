package com.vt.shoppet.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import com.vt.shoppet.repo.LabelerRepo
import kotlinx.coroutines.Dispatchers

class LabelerViewModel @ViewModelInject constructor(
    private val labeler: LabelerRepo
) : ViewModel() {

    fun process(image: FirebaseVisionImage): LiveData<Result<List<FirebaseVisionImageLabel>>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                labeler.process(image)
            }
            emit(result)
        }

}