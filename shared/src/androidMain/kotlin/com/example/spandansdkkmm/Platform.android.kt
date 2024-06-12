// AndroidModule.kt
package com.example.spandansdkkmm

import android.app.Application
import android.util.Log
import com.example.spandansdkkmm.Interface.ConnectionStateListener
import `in`.sunfox.healthcare.commons.android.sericom.SeriCom
import `in`.sunfox.healthcare.commons.android.sericom.interfaces.OnConnectionStateChangeListener
import `in`.sunfox.healthcare.commons.android.sericom.utils.DeviceErrorState

class AndroidPlatform : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}
class AndroidListener:InitializeListener{

    override fun setOnConnectionStateListener(connectionStateListener: ConnectionStateListener) {
        val onConnectionStateChangeListener: OnConnectionStateChangeListener= object : OnConnectionStateChangeListener{


            override fun onConnectionError(errorCode: DeviceErrorState, errorMessage: String?) {

                Log.d("ANDROID_SERICOM", "Error $errorCode")
                when (errorCode) {
                    DeviceErrorState.CONNECTION -> {
                        connectionStateListener.onConnectionError(com.example.spandansdkkmm.enums.DeviceErrorState.CONNECTION)
                    }
                    DeviceErrorState.PERMISSION_DENIED -> {
                        connectionStateListener.onConnectionError(com.example.spandansdkkmm.enums.DeviceErrorState.PERMISSIONDENIED)
                    }

                    DeviceErrorState.ENDPOINT -> {
                        connectionStateListener.onConnectionError(com.example.spandansdkkmm.enums.DeviceErrorState.ENDPOINT)
                    }
                    DeviceErrorState.USB_REQUEST -> {
                        connectionStateListener.onConnectionError(com.example.spandansdkkmm.enums.DeviceErrorState.USB_REQUEST)
                    }
                }
            }

            override fun onDeviceAttached() {
                Log.d("ANDROID_SERICOM", "Device is attached")
                connectionStateListener.onDeviceAttached()
            }

            override fun onDeviceConnected() {
                Log.d("ANDROID_SERICOM", "Device is Connected")
                connectionStateListener.onDeviceConnected()
            }

            override fun onDeviceDisconnected() {
                Log.d("ANDROID_SERICOM", "Device is DISConnected")
                connectionStateListener.onDeviceDisconnected()
            }

            override fun onReceivedData(data: String) {
                Log.d("ANDROID_SERICOM", "received $data")
                connectionStateListener.onReceivedData(data)
            }
        }
        SeriCom.setOnConnectionChangeListener(onConnectionStateChangeListener)

    }

}

class AndroidInitializer : Initializer {
    override fun initialize(context: Any) {
        // Android specific initialization using the provided context
        val application = context as Application
        Log.w("TEST", "initialize: RECEIVED", )
        SeriCom.initialize(application)

        Log.w("TEST", "initialize: ${SeriCom.isDeviceConnected()}" )

    }
}
class AndroidCommunicator : Communicate {
    override fun sendCommand(command: String) {
        SeriCom.sendCommand(command)
        // iOS specific code for sending commands
    }

    override fun getDeviceConnected(): Boolean {
       return  SeriCom.isDeviceConnected()
    }
}


actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getInitializer(): Initializer = AndroidInitializer()

actual fun getCommunicator(): Communicate = AndroidCommunicator()
actual fun setListener():InitializeListener = AndroidListener()