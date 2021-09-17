package com.vt.shoppet.model

data class Filter(
    val enabled: Boolean = false,
    val type: List<String> = listOf("Bird", "Cat", "Dog", "Fish", "Lizard"),
    val sex: List<String> = listOf("Male", "Female"),
    val price: Boolean = false,
    val amounts: List<Float> = listOf(0F, 10000F),
    val age: String = "No Filter",
    val ages: List<Float> = listOf(0F, 100F),
    val field: String = "Upload Date",
    val order: Boolean = false
)