package com.vt.shoppet.impl

import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions
import com.vt.shoppet.repo.VisionRepo
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VisionRepoImpl @Inject constructor(
    vision: FirebaseVision,
    options: FirebaseVisionOnDeviceImageLabelerOptions
) : VisionRepo {

    private val labeler = vision.getOnDeviceImageLabeler(options)

    override suspend fun process(image: FirebaseVisionImage): List<FirebaseVisionImageLabel> =
        labeler.processImage(image).await()

}