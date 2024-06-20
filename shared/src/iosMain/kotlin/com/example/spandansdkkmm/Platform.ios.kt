
package com.example.spandansdkkmm

import AuthenticationHelperIOS
import cocoapods.SericomPod.DeviceErrorState
import cocoapods.SericomPod.OnConnectionStateChangeListenerProtocol
import cocoapods.SericomPod.SeriCom
import com.example.spandansdkkmm.listener.ConnectionStateListener
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import platform.darwin.NSObject
import io.ktor.client.engine.darwin.*
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

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
                        connectionStateListener.onConnectionError(com.example.spandansdkkmm.enums.DeviceErrorState.PERMISSION_DENIED)
                    }

                    2L -> {
                        connectionStateListener.onConnectionError(com.example.spandansdkkmm.enums.DeviceErrorState.ENDPOINT)
                    }
                    3L ->{
                        connectionStateListener.onConnectionError(com.example.spandansdkkmm.enums.DeviceErrorState.BLUETOOTH_NOT_POWERED_ON)
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

//                print("receievec"+data)
                    connectionStateListener.onReceivedData(data)
//                }
            }
        }
        SeriCom.setOnConnectionChangeListener(onConnectionStateChangeListenerProtocol)
    }

}
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

    override fun clearInstance() {

    }
}
class Authentication : AuthenticationHelper {
    private val helper = AuthenticationHelperIOS()

    override fun decrypt(strToDecrypt: String?, key: String, iv: String): String {
        return helper.decrypt(strToDecrypt,key,iv)
    }

    override fun init(stringToHash: String):String {
        return helper.init(stringToHash)
    }
}





actual fun getPlatform(): Platform = IOSPlatform()

actual fun getInitializer(): Initializer = IOSInitializer()

actual fun getCommunicator(): Communicate = IOSCommunicator()
actual fun setListener():InitializeListener = IOSListener()

actual fun authenticationHelper():AuthenticationHelper = Authentication()


actual fun httpClient(config: HttpClientConfig<*>.() -> Unit) = HttpClient(Darwin) {
    config(this)
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.BODY
    }
    install(ContentNegotiation) {

        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true

        })
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 15000L// 2 minutes in milliseconds
    }
    defaultRequest {
        contentType(ContentType.Application.Json)
        accept(ContentType.Application.Json)
    }
    HttpResponseValidator {
        validateResponse { response ->
            val statusCode = response.status.value
            if (statusCode !in 200..299) {
                throw ClientRequestException(response, response.status.description)
            }
        }
    }
    engine {
        configureRequest {
            setAllowsCellularAccess(true)
        }

    }
}


