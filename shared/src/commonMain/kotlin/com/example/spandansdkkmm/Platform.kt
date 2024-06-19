// CommonModule.kt
package com.example.spandansdkkmm

import com.example.spandansdkkmm.listener.ConnectionStateListener
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

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

    fun clearInstance()
}

interface  AuthenticationHelper{
    fun decrypt(strToDecrypt: String?, key: String, iv: String):String
    fun init(stringToHash:String):String
}
expect fun getPlatform(): Platform
expect fun getInitializer(): Initializer
expect fun getCommunicator(): Communicate
expect fun setListener():  InitializeListener

expect fun authenticationHelper():AuthenticationHelper
expect fun httpClient(config: HttpClientConfig<*>.() -> Unit = {}): HttpClient