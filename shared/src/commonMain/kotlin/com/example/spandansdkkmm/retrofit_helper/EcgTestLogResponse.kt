package com.example.spandansdkkmm.retrofit_helper

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class EcgTestLogResponse(
    @SerialName("message")
    var message: String,
    @SerialName("status")
    var status: String? = null,
)