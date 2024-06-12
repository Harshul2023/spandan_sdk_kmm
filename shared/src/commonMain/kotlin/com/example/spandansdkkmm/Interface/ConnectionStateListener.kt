package com.example.spandansdkkmm.Interface

import com.example.spandansdkkmm.enums.DeviceErrorState

interface ConnectionStateListener {
    fun onConnectionError(errorCode: DeviceErrorState)
    fun onDeviceAttached()
    fun onDeviceConnected()
    fun onDeviceDisconnected()
    fun onReceivedData(data: String)
}