package com.example.spandansdkkmm

interface OnInitializationCompleteListener {
    fun onInitializationSuccess(authenticationToken: String)
    fun onInitializationFailed(message: String)
}