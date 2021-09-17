package com.vt.shoppet.repo

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeler
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@ActivityRetainedScoped
class VisionRepo @Inject constructor(private val labeler: ImageLabeler) {

    suspend fun process(image: InputImage): List<ImageLabel> =
        labeler.process(image).await()

}