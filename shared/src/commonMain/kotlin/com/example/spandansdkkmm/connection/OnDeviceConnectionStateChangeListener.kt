package com.example.spandansdkkmm.connection

import DeviceInfo


interface OnDeviceConnectionStateChangeListener {
    fun onDeviceAttached()
    fun onDeviceConnected(deviceInfo: DeviceInfo)
    fun onDeviceDisconnected()
    fun onUsbPermissionDenied()
    fun onConnectionTimedOut()
}