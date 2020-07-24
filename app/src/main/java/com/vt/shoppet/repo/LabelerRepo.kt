package com.vt.shoppet.repo

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel

interface LabelerRepo {
    suspend fun process(image: InputImage): List<ImageLabel>
}