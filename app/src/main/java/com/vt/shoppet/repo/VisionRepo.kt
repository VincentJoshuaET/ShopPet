package com.vt.shoppet.repo

import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel

interface VisionRepo {
    suspend fun process(image: FirebaseVisionImage): List<FirebaseVisionImageLabel>
}