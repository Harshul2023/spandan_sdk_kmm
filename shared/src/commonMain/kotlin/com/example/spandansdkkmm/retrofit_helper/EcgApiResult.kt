package com.example.spandansdkkmm.retrofit_helper

data class EcgApiResult(
    val characteristics: Characteristics,
    val conclusions: Any,
    val success: Boolean,
    val url: String
)