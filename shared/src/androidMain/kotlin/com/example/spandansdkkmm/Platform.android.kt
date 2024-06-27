// AndroidModule.kt
package com.example.spandansdkkmm

import android.app.Application
import android.util.Log
import com.example.spandansdkkmm.listener.ConnectionStateListener
import `in`.sunfox.healthcare.commons.android.sericom.SeriCom
import `in`.sunfox.healthcare.commons.android.sericom.interfaces.OnConnectionStateChangeListener
import `in`.sunfox.healthcare.commons.android.sericom.utils.DeviceErrorState
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import java.util.concurrent.TimeUnit
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
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
                        connectionStateListener.onConnectionError(com.example.spandansdkkmm.enums.DeviceErrorState.PERMISSION_DENIED)
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

    override fun clearInstance() {
        TODO("Not yet implemented")
    }
}
class Authentication : AuthenticationHelper {
    private val helper = AuthenticationHelperANDROID()

    override fun decrypt(strToDecrypt: String?, key: String, iv: String): String {
        return helper.decrypt(strToDecrypt,key,iv)
    }

    override fun init(stringToHash: String):String {
        return helper.init(stringToHash)
    }
}

actual fun authenticationHelper():AuthenticationHelper = Authentication()

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getInitializer(): Initializer = AndroidInitializer()

actual fun getCommunicator(): Communicate = AndroidCommunicator()
actual fun setListener():InitializeListener = AndroidListener()
actual fun httpClient(config: HttpClientConfig<*>.() -> Unit) = HttpClient(OkHttp) {
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
//                requestTimeoutMillis = 2 * 60 * 1000 // 2 minutes in milliseconds
        requestTimeoutMillis = 15000L// 2 minutes in milliseconds
    }

    install(DefaultRequest) {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
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
        config {
            retryOnConnectionFailure(true)
            connectTimeout(0, TimeUnit.SECONDS)
        }
    }
}