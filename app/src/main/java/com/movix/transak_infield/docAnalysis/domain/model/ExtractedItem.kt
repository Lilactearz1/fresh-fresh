package com.movix.transak_infield.docAnalysis.domain.model

data class ExtractedItem(
    val id: Int,
    var description: String,
    var quantity: Double?,
    var price: Double?,
    val confidence: Double
)