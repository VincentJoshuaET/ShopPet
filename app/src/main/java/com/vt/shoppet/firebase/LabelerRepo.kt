package com.vt.shoppet.firebase

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel

interface LabelerRepo {
    suspend fun process(image: InputImage): List<ImageLabel>
}