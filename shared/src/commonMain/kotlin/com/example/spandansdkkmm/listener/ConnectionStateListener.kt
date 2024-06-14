package com.example.spandansdkkmm.listener

import com.example.spandansdkkmm.enums.DeviceErrorState

interface ConnectionStateListener {
    fun onConnectionError(errorCode: DeviceErrorState)
    fun onDeviceAttached()
    fun onDeviceConnected()
    fun onDeviceDisconnected()
    fun onReceivedData(data: String)
}