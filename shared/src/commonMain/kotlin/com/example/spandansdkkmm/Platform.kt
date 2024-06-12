// CommonModule.kt
package com.example.spandansdkkmm

import com.example.spandansdkkmm.Interface.ConnectionStateListener

interface Platform {
    val name: String
}

interface Initializer {
    fun initialize(context: Any)
}

interface InitializeListener{
    fun setOnConnectionStateListener(connectionStateListener: ConnectionStateListener)
}
interface Communicate {
    fun sendCommand(command: String)
    fun getDeviceConnected(): Boolean
}

expect fun getPlatform(): Platform
expect fun getInitializer(): Initializer
expect fun getCommunicator(): Communicate
expect fun setListener():  InitializeListener


