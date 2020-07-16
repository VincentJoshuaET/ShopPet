package com.vt.shoppet.impl

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeler
import com.vt.shoppet.firebase.LabelerRepo
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LabelerRepoImpl @Inject constructor(private val labeler: ImageLabeler) : LabelerRepo {

    override suspend fun process(image: InputImage): List<ImageLabel> =
        labeler.process(image).await()

}