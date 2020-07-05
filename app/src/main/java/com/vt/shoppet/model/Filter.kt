package com.vt.shoppet.model

data class Filter(
    var enabled: Boolean = false,
    var type: String = "All",
    var sex: String = "Both",
    var price: String = "No Filter",
    var amounts: List<Float> = listOf(0F, 10000F),
    var age: String = "No Filter",
    var ages: List<Float> = listOf(0F, 100F),
    var field: String = "Upload Date",
    var order: String = "Descending"
)