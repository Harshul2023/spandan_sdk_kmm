package com.example.spandansdkkmm.listener

interface ConnectionStateListener {
    fun onConnectionError(errorCode: DeviceErrorState)
    fun onDeviceAttached()
    fun onDeviceConnected()
    fun onDeviceDisconnected()
    fun onReceivedData(data: String)
}