package com.example.spandansdkkmm.retrofit_helper

import kotlinx.serialization.Serializable

@Serializable
data class Characteristics(
    val heartRate: Int,
    val pr: Int,
    val qrs: Int,
    val qt: Int,
    val qtc: Double,
    val rr: Int,
    val st: Double
)