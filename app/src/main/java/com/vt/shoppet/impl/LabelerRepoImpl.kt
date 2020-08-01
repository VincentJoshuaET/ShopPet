package com.vt.shoppet.impl

import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler
import com.vt.shoppet.repo.LabelerRepo
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LabelerRepoImpl @Inject constructor(private val labeler: FirebaseVisionImageLabeler) :
    LabelerRepo {

    override suspend fun process(image: FirebaseVisionImage): List<FirebaseVisionImageLabel> =
        labeler.processImage(image).await()

}