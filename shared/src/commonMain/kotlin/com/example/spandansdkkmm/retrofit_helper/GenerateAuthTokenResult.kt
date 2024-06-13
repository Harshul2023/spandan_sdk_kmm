package com.example.spandansdkkmm.retrofit_helper

import kotlinx.serialization.SerialName

data class GenerateAuthTokenResult(
    @SerialName("message")
    var message: String,
    @SerialName("token")
    var token: String,
    @SerialName("created_at")
    var createdAt:String,
    @SerialName("id")
    var id:String,
    @SerialName("status")
    var status:String,
    @SerialName("success")
    var success:Boolean
)