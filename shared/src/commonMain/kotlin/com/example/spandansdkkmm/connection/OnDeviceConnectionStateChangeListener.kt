package com.example.spandansdkkmm.connection

import `in`.sunfox.healthcare.commons.android.spandan_sdk.connection.DeviceInfo

interface OnDeviceConnectionStateChangeListener {
    fun onDeviceAttached()
    fun onDeviceConnected(deviceInfo: DeviceInfo)
    fun onDeviceDisconnected()
    fun onUsbPermissionDenied()
    fun onConnectionTimedOut()
    fun onDataReceived(data:String)
}