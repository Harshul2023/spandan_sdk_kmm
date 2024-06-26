package com.example.spandansdkkmm.retrofit_helper

import kotlinx.serialization.Serializable

@Serializable
data class Conclusions(
    val anomalies: String? = null,
    val detection: String,
    val ecgType: String,
    val recommendation: String? = null,
    val risk: String? = null
)