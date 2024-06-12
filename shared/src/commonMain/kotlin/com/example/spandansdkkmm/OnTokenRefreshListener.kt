package com.example.spandansdkkmm

interface OnTokenRefreshListener {
    fun onTokenRefreshSuccess(authenticationToken: String)
    fun onTokenRefreshFailed(message: String)
}