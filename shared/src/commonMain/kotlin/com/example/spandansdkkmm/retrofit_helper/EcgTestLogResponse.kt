package com.example.spandansdkkmm.retrofit_helper

import kotlinx.serialization.SerialName

class EcgTestLogResponse(
    @SerialName("message")
    var message: String,
    @SerialName("status")
    var status:String,
)