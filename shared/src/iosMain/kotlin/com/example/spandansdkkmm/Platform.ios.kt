
package com.example.spandansdkkmm

import cocoapods.SericomPod.DeviceErrorState
import cocoapods.SericomPod.OnConnectionStateChangeListenerProtocol
import cocoapods.SericomPod.SeriCom
import com.example.spandansdkkmm.Interface.ConnectionStateListener
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import platform.darwin.NSObject

class IOSPlatform : Platform {
    override val name: String = "iOS"
}

class IOSListener:InitializeListener{
    @OptIn(ExperimentalForeignApi::class)
    override fun setOnConnectionStateListener(connectionStateListener: ConnectionStateListener) {
        val onConnectionStateChangeListenerProtocol: OnConnectionStateChangeListenerProtocol = object : NSObject(), OnConnectionStateChangeListenerProtocol {
            override fun onConnectionError(errorCode: DeviceErrorState) {
                when (errorCode) {
                    0L -> {
                        connectionStateListener.onConnectionError(com.example.spandansdkkmm.enums.DeviceErrorState.CONNECTION)
                    }
                    1L -> {
                        connectionStateListener.onConnectionError(com.example.spandansdkkmm.enums.DeviceErrorState.PERMISSIONDENIED)
                    }

                    2L -> {
                        connectionStateListener.onConnectionError(com.example.spandansdkkmm.enums.DeviceErrorState.ENDPOINT)
                    }
                    3L ->{
                        connectionStateListener.onConnectionError(com.example.spandansdkkmm.enums.DeviceErrorState.BLUETOOTHNOTPOWEREDON)
                    }
                }
            }

            override fun onDeviceAttached() {
                print("device attached")
               connectionStateListener.onDeviceAttached()
            }

            override fun onDeviceConnected() {
                connectionStateListener.onDeviceConnected()
            }

            override fun onDeviceDisconnected() {
                print("DISCONNECTED")
                connectionStateListener.onDeviceDisconnected()
            }

            override fun onReceivedData(data: String) {

                    connectionStateListener.onReceivedData(data)
//                }
            }
        }
        SeriCom.setOnConnectionChangeListener(onConnectionStateChangeListenerProtocol)
    }

}
//fun hexToString(hex: String): String {
//    val bytes = mutableListOf<Byte>()
//    for (i in hex.indices step 2) {
//        val byteString = hex.substring(i, i + 2)
//        val byte = byteString.toInt(16).toByte()
//        bytes.add(byte)
//    }
//    return bytes.toByteArray().decodeToString()
//}



class IOSInitializer : Initializer {
    @OptIn(ExperimentalForeignApi::class)
    override fun initialize(context: Any) {
        // iOS specific initialization using the provided context
        val uiApplication = context as UIApplication
        SeriCom.initialize(uiApplication)
    }
}

class IOSCommunicator : Communicate {
    @OptIn(ExperimentalForeignApi::class)
    override fun sendCommand(command: String) {
        SeriCom.sendCommand(command)

    }

    @OptIn(ExperimentalForeignApi::class)
    override fun getDeviceConnected(): Boolean {
        return SeriCom.isDeviceConnected()
    }
}




actual fun getPlatform(): Platform = IOSPlatform()

actual fun getInitializer(): Initializer = IOSInitializer()

actual fun getCommunicator(): Communicate = IOSCommunicator()
actual fun setListener():InitializeListener = IOSListener()
