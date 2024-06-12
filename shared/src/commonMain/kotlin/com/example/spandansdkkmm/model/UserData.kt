package com.example.spandansdkkmm.model

data class UserData(
    val firstName:String,
    val lastName:String,
    val age:String,
    val height:String,
    val weight:String,
    val gender:String,
    val phoneNumber: String,
    val temp:String?=null,
    val bp:String?=null,
    val spo2:String?=null
)