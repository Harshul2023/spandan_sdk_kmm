package com.example.spandansdkkmm

import AuthenticationHelper
import DeviceInfo
import EcgTest
import GenerateReportModel
import MixPanelHelper
import OnReportGenerationStateListener
import PDFReportGenerationCallback
import RetrofitHelper
import com.example.spandansdkkmm.Const.ERROR_TEST_NOT_VALID
import com.example.spandansdkkmm.collection.EcgTestCallback
import com.example.spandansdkkmm.conclusion.EcgReport
import com.example.spandansdkkmm.connection.OnDeviceConnectionStateChangeListener
import com.example.spandansdkkmm.connection.SpandanSDKException
import com.example.spandansdkkmm.enums.DeviceErrorState
import com.example.spandansdkkmm.enums.EcgPosition
import com.example.spandansdkkmm.enums.EcgTestType
import com.example.spandansdkkmm.enums.SpandanDeviceVariant
import com.example.spandansdkkmm.listener.ConnectionStateListener
import com.example.spandansdkkmm.retrofit_helper.ApiEcgData
import com.example.spandansdkkmm.retrofit_helper.GeneratePdfReportInputData
import com.example.spandansdkkmm.retrofit_helper.MetaData
import com.example.spandansdkkmm.retrofit_helper.PatientData
import com.example.spandansdkkmm.util.Utility
import io.ktor.util.encodeBase64
import io.ktor.utils.io.core.toByteArray
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.datetime.Clock
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.encodeUtf8
import kotlin.concurrent.Volatile
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.jvm.JvmStatic
import kotlin.random.Random
import saschpe.kase64.*


class SpandanSDK private constructor() {
    private val platform: Platform = getPlatform()
    private var onDeviceConnectionStateChangeListener: OnDeviceConnectionStateChangeListener? = null
    private lateinit var report: EcgReport
    private lateinit var ecgTestType: EcgTestType
    private lateinit var ecgTestInstance: EcgTest
    private lateinit var authenticationHelper: AuthenticationHelper
    private var enabledEcgTests: List<EcgTestType>? = null
    private var enabledSpandanDeviceVariants: List<SpandanDeviceVariant> = arrayListOf()
    private lateinit var application: Any
    private var useOfflineToken = false
    private var isDeviceVerified = false
    private var deviceInfo: DeviceInfo? = null
    private var sendCommand = ""
    private val TAG = "SpandanSDK.TAG"



    companion object {
        @Volatile
        private var INSTANCE: SpandanSDK? = null

        @Volatile
        private var sessionId: String? = null

        @Volatile
        private var mixPanelHelper: MixPanelHelper? = null

        @Volatile
        private lateinit var masterKey: String

        @Volatile
        private var verifierToken: String? = null

        @Volatile
        private var generatedAuthToken: String? = null

        @JvmStatic
        fun getInstance(): SpandanSDK {
            return INSTANCE!!
        }

        @JvmStatic
        private fun getSessionId() = sessionId



        fun base64Encoded(input: String): String = input.encodeUtf8().base64()

        fun decodeBase64ToString(input: String): String = input.decodeBase64()!!.utf8()


        public fun initializeOffline(application: Any, token: String) {
//            CoroutineScope(Dispatchers.Main).launch {
//                val mutex = Mutex()
//                mutex.withLock {
                    masterKey = token
//                    mixPanelHelper = MixPanelHelper.getInstance(application)
                    if (INSTANCE == null) {
                        INSTANCE = SpandanSDK()
                        INSTANCE!!.bind(application)
                        val w = decodeBase64ToString(token)
                        val id = w.substring(0, 64)
                        val createdAt = w.substring(64, 77)
                        val masterKey = w.substring(77, 93)
                        generatedAuthToken = w.substring(93, w.length)
                        INSTANCE!!.authenticationHelper =
                            AuthenticationHelper(
                                id = id,
                                createdAt = createdAt,
                                masterKey = masterKey
                            )
                        if (INSTANCE!!.validateOfflineAuthKey(generatedAuthToken!!)) INSTANCE!!.bind(application)
                        else throw SpandanSDKException("${SpandanException.SDKNotInitialisedException.name}: Could not initialise Spandan SDK. The token is invalid. Please check the token and try again.")
                    }
//                }
//            }
        }



        public fun initialize(
            application: Any,
            verifierToken: String,
            masterKey: String,
            onInitializationCompleteListener: OnInitializationCompleteListener,
        ) {
            /**
             * @param organizationUniqueId can be used to uniquely identified the package name.**/

//launchval mutex = Mutex()
//            mutex.withLock {
//                mixPanelHelper = MixPanelHelper.getInstance(application)
                SpandanSDK.masterKey = masterKey
                SpandanSDK.verifierToken = verifierToken
                if (INSTANCE == null) {
                    INSTANCE = SpandanSDK()
                    INSTANCE!!.bind(application)
                    CoroutineScope(Dispatchers.IO).launch {
                        val sessionId = INSTANCE!!.createSessionId()
                        val response = RetrofitHelper().getRetrofitInstance().getAuthToken(
                            authorization = verifierToken,
                            apiKey = masterKey,
                            sessionId = sessionId!!
                        )

                        try {

                            if (response.success) {
                                val authTokenResult = response.token
                                if (authTokenResult != null) {
//                                    if (authTokenResult.success) {
//                                    mixPanelHelper.sendToMixpanel(
//                                        eventName = SDK_INITIALISE_COMPLETE,
//                                        key = arrayListOf(MASTER_KEY),
//                                        value = arrayListOf(masterKey)
//                                    )
                                    generatedAuthToken = authTokenResult
                                    INSTANCE!!.authenticationHelper = AuthenticationHelper(
                                        response.id,
                                        response.createdAt,
                                        masterKey
                                    )

                                    onInitializationCompleteListener.onInitializationSuccess()
                                    try {
                                        INSTANCE!!.decodeDeviceVariantAndTestType(generatedAuthToken!!)
                                    } catch (e: Exception) {
                                        onInitializationCompleteListener.onInitializationFailed(
                                            "${SpandanException.InvalidSessionException.name}: The session is not valid. Please re-initialise the Spandan SDK. Error Code: 4"
                                        )
                                    }
//                                    } else {
//                                        mixPanelHelper.sendToMixpanel(
//                                            eventName = SDK_INITIALISE_FAILED,
//                                            key = arrayListOf(
//                                                MASTER_KEY,
//                                                REASON
//                                            ),
//                                            value = arrayListOf(
//                                                masterKey,
//                                                authTokenResult.message
//                                            )
//                                        )
//                                        onInitializationCompleteListener.onInitializationFailed(authTokenResult.message)
//                                    }
                                } else {
//                                    mixPanelHelper.sendToMixpanel(
//                                        eventName = SDK_INITIALISE_FAILED,
//                                        key = arrayListOf(MASTER_KEY, REASON),
//                                        value = arrayListOf(
//                                            masterKey,
//                                            "Internal server error. Please contact Sunfox support team"
//                                        )
//                                    )
                                    onInitializationCompleteListener.onInitializationFailed(
                                        "Internal server error. Please contact Sunfox support team"
                                    )
                                }
                            } else {
                                val errorBody = response.message.toString()
//                                val errorMsg = when (response.code()) {
//                                    401 -> Gson().fromJson(errorBody, ErrorResponse::class).message
//                                    else -> "Internal server error. Please contact Sunfox support team"
//                                }

//                                mixPanelHelper.sendToMixpanel(
//                                    eventName = SDK_INITIALISE_FAILED,
//                                    key = arrayListOf(
//                                        MASTER_KEY,
//                                        REASON
//                                    ),
//                                    value = arrayListOf(
//                                        masterKey,
//                                        errorBody
//                                    )
//                                )
                                onInitializationCompleteListener.onInitializationFailed(errorBody)
                            }
                        } catch (e: IOException) {
//                            Log.d("Spandan.TAG", "onFailure: ${e.toString()}")
//                            mixPanelHelper.sendToMixpanel(
//                                eventName = SDK_INITIALISE_FAILED,
//                                key = arrayListOf(
//                                    MASTER_KEY,
//                                    REASON
//                                ),
//                                value = arrayListOf(
//                                    masterKey,
//                                    "Initialization failed: ${e.message ?: "Unknown error"}"
//                                )
//                            )
                            onInitializationCompleteListener.onInitializationFailed(
                                "Initialization failed: ${e.message ?: "Unknown error"}"
                            )
                        }
                    }


//            }
            }
        }

        @Volatile
        private var isDeviceConnected = false
    }


    fun setOnDeviceConnectionStateChangedListener(onDeviceConnectionStateChangeListener: OnDeviceConnectionStateChangeListener) {
        this.onDeviceConnectionStateChangeListener = onDeviceConnectionStateChangeListener
    }

    fun bind(application: Any) {
        getInitializer().initialize(application)
        this.application = application
        if (isDeviceConnected()) {
            isDeviceConnected = true
        }

        val connectionStateListener=object :ConnectionStateListener{
            override fun onConnectionError(errorCode: DeviceErrorState) {
                when (errorCode) {
                    DeviceErrorState.CONNECTION -> {}
                    DeviceErrorState.PERMISSION_DENIED -> {
                        onDeviceConnectionStateChangeListener?.onUsbPermissionDenied()
                    }

                    DeviceErrorState.ENDPOINT -> {}
                    DeviceErrorState.USB_REQUEST -> {}
                    DeviceErrorState.BLUETOOTH_NOT_POWERED_ON ->{}
                }
            }

            override fun onDeviceAttached() {
                isDeviceConnected = true
                onDeviceConnectionStateChangeListener?.onDeviceAttached()
            }

            override fun onDeviceConnected() {
//                mixPanelHelper.sendToMixpanel(
//                    eventName = DEVICE_CONNECTED,
//                    key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE),
//                    value = arrayListOf(
//                        masterKey,
//                        if (getDeviceVariantString() != "") getDeviceVariantString() else {
//                            "device not verified yet."
//                        }
//                    )
//                )
                if(platform.name.contains("iOS")){
                    GlobalScope.launch(Dispatchers.Main) {
                        delay(2000)
                        sendCommand = "c"
                        getCommunicator().sendCommand(sendCommand)
//                    }
                    }

                }
                else{
                    sendCommand = "c"
                    getCommunicator().sendCommand(sendCommand)
                }


//                val handler1 = Handler(Looper.getMainLooper())
//                handler1.postDelayed({
//                    if (!isDeviceVerified) {
//                        mixPanelHelper.sendToMixpanel(
//                            eventName = DEVICE_CONNECTION_TIMEOUT,
//                            key = arrayListOf(
//                                MASTER_KEY
////                                , CONNECTED_DEVICE_TYPE
//                            ),
//                            value = arrayListOf(
//                                masterKey,
////                                if (getVariant() != "") getVariant() else {
////                                    "device not verified yet."
////                                }
//                            )
//                        )
//                        onDeviceConnectionStateChangeListener?.onConnectionTimedOut()
//                    }
//                }, 5000)
                GlobalScope.launch(Dispatchers.Main) {
                    delay(5000)
//                    if (!isDeviceVerified) {
//                        mixPanelHelper.sendToMixpanel(
//                            eventName = DEVICE_CONNECTION_TIMEOUT,
//                            key = arrayListOf(
//                                MASTER_KEY
//                                // , CONNECTED_DEVICE_TYPE
//                            ),
//                            value = arrayListOf(
//                                masterKey
//                                // if (getVariant() != "") getVariant() else {
//                                //     "device not verified yet."
//                                // }
//                            )
//                        )
//                        onDeviceConnectionStateChangeListener?.onConnectionTimedOut()
//                    }
                }
            }

            override fun onDeviceDisconnected() {
                val deviceVariant = getDeviceVariantString()
//                mixPanelHelper.sendToMixpanel(
//                    eventName = DEVICE_DISCONNECTED,
//                    key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE),
//                    value = arrayListOf(
//                        masterKey,
//                        if (deviceVariant != "") deviceVariant else {
//                            "device not verified yet."
//                        }
//                    )
//                )
                isDeviceConnected = false
                isDeviceVerified = false

                onDeviceConnectionStateChangeListener?.onDeviceDisconnected()
                if (::ecgTestInstance.isInitialized)
                    ecgTestInstance.onDeviceDisconnected()
            }

            override fun onReceivedData(data: String) {
                if(sendCommand =="c" && platform.name.contains("iOS")) {

                    populateDeviceInfo(convertHexToAsciiAndKeepHex(data))
                }
                else
                    populateDeviceInfo(data)
            }

        }
        setListener().setOnConnectionStateListener(connectionStateListener=connectionStateListener)
    }
    fun convertHexToAsciiAndKeepHex(hex: String): String {
        val parts = hex.split("2d") // Split the hex string at each occurrence of "2D"
        val result = StringBuilder()
        for (i in parts.indices) {
            if (i < parts.size - 1) {
                // Convert each part to ASCII text except the last one
                result.append(parts[i].chunked(2).map { it.toInt(16).toChar() }.joinToString(""))
                result.append("-")
            } else {
                // Keep the last part as hex
                result.append(parts[i])
            }
        }
        return result.toString()
    }
    private fun getDeviceVariantString(): String {
        var deviceVariant = ""
        deviceInfo.let {
            if (it != null) {
                deviceVariant = it.deviceVariant.name
            }
        }
        return deviceVariant
    }

    fun createTest(
        ecgTestType: EcgTestType,
        ecgTestCallback: EcgTestCallback,
    ): EcgTest {
        if (generatedAuthToken == null) {
            throw SpandanSDKException(SpandanException.SDKNotInitialisedException.name + ": SDK not initialized. Please initialise the SDK before creating the test.")
        } else {
            if (useOfflineToken) {
                if (!validateOfflineAuthKey(generatedAuthToken!!)) {
                    val exception =
                        SpandanSDKException("${SpandanException.InvalidSessionException.name}: The session is invalid. Please re-initialize the Spandan SDK")
//                    mixPanelHelper.sendToMixpanel(
//                        eventName = TEST_CREATE_FAILED,
//                        key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE, REASON),
//                        value = arrayListOf(
//                            masterKey,
//                            getDeviceVariantString(),
//                            ecgTestType.name,
//                            exception.toString()
//                        )
//                    )
                    throw exception
                }
            } else {
                if (!validateAuthKey(generatedAuthToken!!)) {
                    val exception =
                        SpandanSDKException("${SpandanException.InvalidSessionException.name}: The session is invalid. Please re-initialize the Spandan SDK")
//                    mixPanelHelper.sendToMixpanel(
//                        eventName = TEST_CREATE_FAILED,
//                        key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE, REASON),
//                        value = arrayListOf(
//                            masterKey,
//                            getDeviceVariantString(),
//                            ecgTestType.name,
//                            exception.toString()
//                        )
//                    )
                    throw exception
                }
            }
        }
//        mixPanelHelper.sendToMixpanel(
//            eventName = TEST_CREATED,
//            key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE),
//            value = arrayListOf(masterKey, getDeviceVariantString(), ecgTestType.name)
//        )
        enabledEcgTests.let {
            if (it != null) {
                if (!it.contains(EcgTestType.HRV) && ecgTestType == EcgTestType.HRV) {
                    val exception =
                        SpandanSDKException("${SpandanException.InvalidTestException.name}: Not authorised for this test. Please contact Sunfox support.")
//                    mixPanelHelper.sendToMixpanel(
//                        eventName = TEST_CREATE_FAILED,
//                        key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE, REASON),
//                        value = arrayListOf(
//                            masterKey,
//                            getDeviceVariantString(),
//                            ecgTestType.name,
//                            exception.toString()
//                        )
//                    )
                    throw exception
                }
                if (!it.contains(EcgTestType.LEAD_TWO) && ecgTestType == EcgTestType.LEAD_TWO) {
                    val exception =
                        SpandanSDKException("${SpandanException.InvalidTestException.name}: Not authorised for this test. Please contact Sunfox support.")
//                    mixPanelHelper.sendToMixpanel(
//                        eventName = TEST_CREATE_FAILED,
//                        key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE, REASON),
//                        value = arrayListOf(
//                            masterKey,
//                            getDeviceVariantString(),
//                            ecgTestType.name,
//                            exception.toString()
//                        )
//                    )
                    throw exception
                }
                if (!it.contains(EcgTestType.TWELVE_LEAD) && ecgTestType == EcgTestType.TWELVE_LEAD) {
                    val exception =
                        SpandanSDKException("${SpandanException.InvalidTestException.name}: Not authorised for this test. Please contact Sunfox support.")
//                    mixPanelHelper.sendToMixpanel(
//                        eventName = TEST_CREATE_FAILED,
//                        key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE, REASON),
//                        value = arrayListOf(
//                            masterKey,
//                            getDeviceVariantString(),
//                            ecgTestType.name,
//                            exception.toString()
//                        )
//                    )
                    throw exception
                }
                if (!it.contains(EcgTestType.HYPERKALEMIA) && ecgTestType == EcgTestType.HYPERKALEMIA) {
                    val exception =
                        SpandanSDKException("${SpandanException.InvalidTestException.name}: Not authorised for this test. Please contact Sunfox support.")
//                    mixPanelHelper.sendToMixpanel(
//                        eventName = TEST_CREATE_FAILED,
//                        key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE, REASON),
//                        value = arrayListOf(
//                            masterKey,
//                            getDeviceVariantString(),
//                            ecgTestType.name,
//                            exception.toString()
//                        )
//                    )
                    throw exception
                }
            }
        }
        deviceInfo.let {
            if (it != null) {
                this.ecgTestType = ecgTestType
                ecgTestInstance =
                    EcgTest(
                        masterKey,
                        ecgTestType,
                        ecgTestCallback,
                        deviceInfo = it,
                        mixPanelHelper,
                        verifierToken
                    )
            } else {
                throw SpandanSDKException(SpandanException.DeviceNotConnectedException.name + ": Spandan device not connected. Please connect the device")
            }
        }
        return ecgTestInstance
    }

    fun isDeviceConnected(): Boolean {
        return getCommunicator().getDeviceConnected()
//        return SeriCom.isDeviceConnected()
    }

    fun unbind() {
        deviceInfo.let {
            if (it != null)
                getCommunicator().sendCommand(
              if (it.deviceVariant == SpandanDeviceVariant.SPANDAN_LEGACY) "0" else if (it.deviceVariant == SpandanDeviceVariant.SPANDAN_NEO) "STP" else "0")
            else
                throw SpandanSDKException(SpandanException.DeviceNotConnectedException.name + ": Spandan device not connected. Please connect the device")
        }
        getCommunicator().clearInstance()

    }

    fun generateReport(
        userAge: Int,
        ecgData: HashMap<EcgPosition, ArrayList<Double>>,
        reportGenerationStatusListener: OnReportGenerationStateListener,
    ) {
        if (!ecgTestInstance.isTestCompleted.value) {
            throw SpandanSDKException(
                SpandanException.TestNotCompleteException.name
                    .plus(": Test not marked as complete. Please complete the test by calling completeTest() method on the test")
            )
        }
        if (generatedAuthToken == null) {
            throw SpandanSDKException(SpandanException.SDKNotInitialisedException.name + ": SDK not initialized. Please initialise the SDK before creating the test.")
        }
        if (useOfflineToken) {
            if (!validateOfflineAuthKey(generatedAuthToken!!)) {
                val exception =
                    SpandanSDKException("${SpandanException.InvalidSessionException.name}: The session is invalid. Please re-initialize the Spandan SDK")
//                mixPanelHelper.sendToMixpanel(
//                    eventName = Const.GENERATE_REPORT_FAILED,
//                    key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE, REASON),
//                    value = arrayListOf(
//                        masterKey,
//                        getDeviceVariantString(),
//                        ecgTestType.name,
//                        exception.toString()
//                    )
//                )
                throw exception
            }
        } else {
            if (!validateAuthKey(generatedAuthToken!!)) {
                val exception =
                    SpandanSDKException("${SpandanException.InvalidSessionException.name}: The session is invalid. Please re-initialize the Spandan SDK")
//                mixPanelHelper.sendToMixpanel(
//                    eventName = Const.GENERATE_REPORT_FAILED,
//                    key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE, REASON),
//                    value = arrayListOf(
//                        masterKey,
//                        getDeviceVariantString(),
//                        ecgTestType.name,
//                        exception.toString()
//                    )
//                )
                throw exception
            }
        }
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            val error = StringBuilder()
            error.append(throwable.message)
            throwable.stackTraceToString().forEach {
                error.append("$it \n")
            }
//            mixPanelHelper.sendToMixpanel(
//                eventName = Const.GENERATE_REPORT_FAILED,
//                key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE, REASON),
//                value = arrayListOf(
//                    masterKey,
//                    getDeviceVariantString(),
//                    ecgTestType.name,
//                    error.toString()
//                )
//            )
            reportGenerationStatusListener.onReportGenerationFailed(
                ERROR_TEST_NOT_VALID,
                "$error"
            )
        }

//        mixPanelHelper.sendTimingEvent(
//            eventName = GENERATE_REPORT_CALLED,
//            key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE),
//            value = arrayListOf(masterKey, getDeviceVariantString(), ecgTestType.name),
//            true
//        )
        CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
            supervisorScope {
                val reportGenerationTask = async {
                    ecgTestInstance.checkForDataValidation(ecgData).let {
                        if (it != null)
                            throw it
                        else {
                            report = ecgTestInstance.proceedReport(
                                userAge,
                                ecgData
                            )
                        }
                    }
                }
                reportGenerationTask.await()
                reportGenerationStatusListener.onReportGenerationSuccess(report)
            }
//            mixPanelHelper.sendTimingEvent(
//                eventName = GENERATE_REPORT_CALLED,
//                key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE),
//                value = arrayListOf(masterKey, getDeviceVariantString(), ecgTestType.name),
//                false
//            )
//            mixPanelHelper.sendToMixpanel(
//                eventName = GENERATE_REPORT_SUCCESS,
//                key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE),
//                value = arrayListOf(masterKey, getDeviceVariantString(), ecgTestType.name)
//            )
        }
    }

    private fun validateAuthKey(authKey: String): Boolean {
        val isAuthKeyValid = !isTokenExpired(authKey, getSessionId())
        return isAuthKeyValid
    }

    private fun decodeDeviceVariantAndTestType(authKey: String) {
        enabledEcgTests = getListOfTestFromAuth(authKey)
        enabledSpandanDeviceVariants = getEnabledDeviceTypeFromAuth(authKey)
    }

    private fun validateOfflineAuthKey(authKey: String): Boolean {
        useOfflineToken = true
        val isAuthKeyValid = !isOfflineTokenExpired(authKey)
        enabledEcgTests = getListOfTestFromAuth(authKey)
        enabledSpandanDeviceVariants = getEnabledDeviceTypeFromAuth(authKey)
        return isAuthKeyValid
    }

    private fun mapDeviceVariantToVariantEnum(deviceVariant: String): SpandanDeviceVariant {
        if (deviceVariant.contains("sppr"))
            return SpandanDeviceVariant.SPANDAN_PRO
        else if (deviceVariant.contains("spne"))
            return SpandanDeviceVariant.SPANDAN_NEO
        else
            return SpandanDeviceVariant.SPANDAN_LEGACY
    }

    private fun populateDeviceInfo(data: String) {
        if (deviceInfo == null) {
            deviceInfo = DeviceInfo()
        }
        if (!isDeviceVerified) {
            val deviceType = enabledSpandanDeviceVariants

            if (data.contains(Regex("-"))) {
                val dataArray = data.split(Regex("-"))
                if (sendCommand == "c") {
                    //check for b2b device
                    if (dataArray[2].substring(0, 4).equals("#b2b", true)) {
//                        if (dataArray[2].substring(4) == masterKey) {
                            isDeviceVerified =
                                (dataArray[0].contains(Regex("spdn")) || dataArray[0].contains(
                                    Regex("splg")
                                ) && deviceType.contains(SpandanDeviceVariant.SPANDAN_LEGACY))
                                        || (dataArray[0].contains(Regex("spne")) && deviceType.contains(
                                    SpandanDeviceVariant.SPANDAN_NEO
                                ))
                                        || (dataArray[0].contains(Regex("sppr")) && deviceType.contains(
                                    SpandanDeviceVariant.SPANDAN_PRO
                                ))

                            deviceInfo.apply {
                                if (this != null) {
                                    deviceVariant = mapDeviceVariantToVariantEnum(dataArray[0])
                                    firmwareVersion = dataArray[1]
                                    deviceMId = dataArray[3]
                                    deviceId = dataArray[2]
                                }
                            }
//                            mixPanelHelper.sendToMixpanel(
//                                eventName = DEVICE_VERIFIED,
//                                key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE),
//                                value = arrayListOf(masterKey, getDeviceVariantString())
//                            )
                            print(deviceInfo)
                            isDeviceVerified = true
                            if (isDeviceVerified) onDeviceConnectionStateChangeListener?.onDeviceConnected(
                                deviceInfo!!
                            )
//                        }
                    } else {
                        //check for normal device
                        isDeviceVerified =
                            (dataArray[0].contains(Regex("spdn")) || dataArray[0].contains(Regex("splg")) && deviceType.contains(
                                SpandanDeviceVariant.SPANDAN_LEGACY
                            ))
                                    || (dataArray[0].contains(Regex("spne")) && deviceType.contains(
                                SpandanDeviceVariant.SPANDAN_NEO
                            ))
                                    || (dataArray[0].contains(Regex("sppr")) && deviceType.contains(
                                SpandanDeviceVariant.SPANDAN_PRO
                            ))

                        deviceInfo.apply {
                            if (this != null) {
                                deviceVariant = mapDeviceVariantToVariantEnum(dataArray[0])
                                firmwareVersion = dataArray[1]

                                /**
                                 *
                                 * add this condition to fetch device_id because in device id is in - separated format.
                                 * example : when command = c then output = spne-001.00-SPNE-DN01-2403280003-8??<??WCF850
                                 * after split dataArray ==> [spne, 001.00, SPNE, DN01, 2403280003, 8??<??WCF850 ]
                                 *
                                 */

                                if (dataArray.size > 4) {
                                    val deviceId = data.substring(
                                        dataArray[0].length + 1 + dataArray[1].length + 1,
                                        dataArray[0].length + 1 + dataArray[1].length + 1 + 20
                                    )
                                    this.deviceId = deviceId
                                    deviceMId = data.substring(
                                        dataArray[0].length + 1 + dataArray[1].length + 1 + deviceId.length + 1,
                                        data.length
                                    )
                                } else {
                                    deviceMId = dataArray[3]
                                    deviceId = dataArray[2]
                                }
                            }
                        }
//                        mixPanelHelper.sendToMixpanel(
//                            eventName = DEVICE_VERIFIED,
//                            key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE),
//                            value = arrayListOf(masterKey, getDeviceVariantString())
//                        )
                        if (isDeviceVerified) onDeviceConnectionStateChangeListener?.onDeviceConnected(
                            deviceInfo!!
                        )

//                        deviceInfo.deviceVariant = dataArray[0]
//                        deviceInfo.firmwareVersion = dataArray[1]
//                        if (dataArray.size > 4) {
//                            val deviceId = data.substring(
//                                dataArray[0].length + 1 + dataArray[1].length + 1,
//                                dataArray[0].length + 1 + dataArray[1].length + 1 + 20
//                            )
//                            deviceInfo.deviceId = deviceId
//                            deviceInfo.deviceMId = data.substring(
//                                dataArray[0].length + 1 + dataArray[1].length + 1 + deviceId.length + 1,
//                                data.length
//                            )
//                        } else {
//                            deviceInfo.deviceMId = dataArray[3]
//                            deviceInfo.deviceId = dataArray[2]
//                        }
//                        mixPanelHelper.sendToMixpanel(
//                            eventName = DEVICE_VERIFIED,
//                            key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE),
//                            value = arrayListOf(masterKey, deviceInfo.deviceVariant)
//                        )
//                        if (isDeviceVerified) onDeviceConnectionStateChangeListener?.onDeviceVerified(
//                            deviceInfo
//                        )
                    }
                    sendCommand = ""
                }
            } else {
                //check for old spandan device which includes only spdn
                isDeviceVerified =
                    data.contains(Regex("spdn")) && deviceType.contains(SpandanDeviceVariant.SPANDAN_LEGACY)
                deviceInfo.apply {
                    if (this != null) {
                        this.deviceVariant = mapDeviceVariantToVariantEnum(data)
                    }
                }
//                mixPanelHelper.sendToMixpanel(
//                    eventName = DEVICE_VERIFIED,
//                    key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE),
//                    value = arrayListOf(masterKey, getDeviceVariantString())
//                )
                if (isDeviceVerified) onDeviceConnectionStateChangeListener?.onDeviceConnected(
                    deviceInfo!!
                )
            }
        }
        if (isDeviceVerified && ::ecgTestInstance.isInitialized) {
            val mData = if (data.contains("\n")) data.replace("\n", "").trim() else data.trim()
            if (mData.isNotEmpty() && mData.all {
                    it.isDigit()
                }
            ) ecgTestInstance.onReceiveData(mData)
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun generatePdfReport(
        ecgTest: EcgTest,
        patientData: PatientData,
        pdfReportGenerationCallback: PDFReportGenerationCallback,
    ) {
        if (generatedAuthToken == null) {
            throw SpandanSDKException(SpandanException.SDKNotInitialisedException.name + ": SDK not initialized. Please initialise the SDK before creating the test.")
        }

        if (!ecgTest.isTestCompleted.value) {
            throw SpandanSDKException(
                SpandanException.TestNotCompleteException.name
                    .plus(": Test not marked as complete. Please complete the test by calling completeTest() method on the test")
            )
        }

        val metaData = MetaData(
            deviceVariant = Utility.mapSpandanVariantToDeviceVariant(ecgTest.deviceInfo.deviceVariant),
            deviceId = ecgTest.deviceInfo.deviceId ?: "",
            firmwareVersion = ecgTest.deviceInfo.firmwareVersion ?: "",
            reportId = ecgTest.uniqueId
        )

        val inputForGenerateReport = GenerateReportModel(
            patientData = patientData,
            generatePdfReport = true,
            processorType = ecgTestType
        )
        var apiEcgData = ApiEcgData()

        when (inputForGenerateReport.processorType) {
            (EcgTestType.TWELVE_LEAD) -> {
                /**
                 * prepare data for the ecg processor api*/
                apiEcgData = ApiEcgData(

//                    v1Data =
//                    Base64.encode(
//                        ecgTest._ecgData[EcgPosition.V1].toString().toByteArray(Charsets.UTF_8), Base64.Default
//                    ),
                    v1Data = encodeToBase64(ecgTest._ecgData[EcgPosition.V1]),

                    v2Data =
                    encodeToBase64(
                        ecgTest._ecgData[EcgPosition.V2]),

                    v3Data =
                    encodeToBase64(
                        ecgTest._ecgData[EcgPosition.V3]
                    ),

                    v4Data =
                    encodeToBase64(
                        ecgTest._ecgData[EcgPosition.V4]
                    ),

                    v5Data =
                    encodeToBase64(
                        ecgTest._ecgData[EcgPosition.V5]
                    ),

                    v6Data =
                    encodeToBase64(
                        ecgTest._ecgData[EcgPosition.V6]
                    ),

                    lead1Data =
                    encodeToBase64(
                        ecgTest._ecgData[EcgPosition.LEAD_1]
                    ),

                    lead2Data =
                    encodeToBase64(
                        ecgTest._ecgData[EcgPosition.LEAD_2]
                    )
                )

            }

            (EcgTestType.HRV) -> {}

            (EcgTestType.LEAD_TWO) -> {
                apiEcgData = ApiEcgData(
                    lead2Data =
                    encodeToBase64(
                        ecgTest._ecgData[EcgPosition.LEAD_2]
                    )
                )
            }

            (EcgTestType.HYPERKALEMIA) -> {

                apiEcgData = ApiEcgData(
                    v1Data =
                    encodeToBase64(
                        ecgTest._ecgData[EcgPosition.V1]
                    ),

                    v2Data =
                    encodeToBase64(
                        ecgTest._ecgData[EcgPosition.V2]
                    ),

                    v3Data =
                    encodeToBase64(
                        ecgTest._ecgData[EcgPosition.V3]
                    ),

                    v4Data =
                    encodeToBase64(
                        ecgTest._ecgData[EcgPosition.V4]
                    ),

                    v5Data =
                    encodeToBase64(
                        ecgTest._ecgData[EcgPosition.V5]
                    ),

                    v6Data =
                    encodeToBase64(
                        ecgTest._ecgData[EcgPosition.V6]
                    ),

                    lead2Data =
                    encodeToBase64(
                        ecgTest._ecgData[EcgPosition.LEAD_2]
                    )

                )

            }

            else -> {}
        }

        val inputForProcessApi = GeneratePdfReportInputData(
            patientData = PatientData(
                age = inputForGenerateReport.patientData.age,
                firstName = inputForGenerateReport.patientData.firstName,
                lastName = inputForGenerateReport.patientData.lastName,
                gender = inputForGenerateReport.patientData.gender,
                height = inputForGenerateReport.patientData.height,
                weight = inputForGenerateReport.patientData.weight,
            ),
            processorType = inputForGenerateReport.processorType.name,
            ecgData = apiEcgData,
            generatePdfReport = true,
            metaData = metaData,
            id = ecgTest.uniqueId
        )
        if (useOfflineToken) {
            if (!validateOfflineAuthKey(generatedAuthToken!!)) {
                val exception =
                    SpandanSDKException("${SpandanException.InvalidSessionException.name}: The session is invalid. Please re-initialize the Spandan SDK")
//                mixPanelHelper.sendToMixpanel(
//                    eventName = Const.GENERATE_REPORT_FAILED,
//                    key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE, REASON),
//                    value = arrayListOf(
//                        masterKey,
//                        getDeviceVariantString(),
//                        ecgTestType.name,
//                        exception.toString()
//                    )
//                )
                throw exception
            }
        } else {
//            mixPanelHelper.sendTimingEvent(
//                eventName = GENERATE_REPORT_CALLED,
//                key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE),
//                value = arrayListOf(masterKey, getDeviceVariantString(), ecgTestType.name),
//                true
//            )

            CoroutineScope(Dispatchers.IO).launch {
                val helper = RetrofitHelper()
                try {
                    val result = helper.getRetrofitInstance()
                        .generatePdfReport(
                            authorization = verifierToken,
                            apiKey = masterKey,
                            generatePdfReportInputData = inputForProcessApi
                        )

                    if (result.success) {

                        val pdfResult = result.data
                        pdfReportGenerationCallback.onReportGenerationSuccess(pdfResult)
                    } else {
                        val errorMsg = result.message
//                        mixPanelHelper.sendToMixpanel(
//                            eventName = Const.GENERATE_REPORT_FAILED,
//                            key = arrayListOf(
//                                MASTER_KEY,
//                                CONNECTED_DEVICE_TYPE,
//                                TEST_TYPE,
//                                REASON
//                            ),
//                            value = arrayListOf(
//                                masterKey,
//                                getDeviceVariantString(),
//                                ecgTestType.name,
//                                errorMsg
//                            )
//                        )
                        pdfReportGenerationCallback.onReportGenerationFailed(errorMsg)
                    }
                } catch (e: Exception) {
                    val errorMsg = "Failed to push ECG test log: ${e.message}"
                    print(errorMsg)
                    // Handle network exceptions or other errors
                }
            }



        }
    }


    private fun isTokenExpired(token: String, sessionId: String?): Boolean {
        val data = JwtDecoder.decode(authenticationHelper.decrypt(token))
        val expClaim = data["exp"]?.jsonPrimitive?.longOrNull
        return expClaim != null && expClaim < Clock.System.now().toEpochMilliseconds() / 1000
    }

    private fun isOfflineTokenExpired(token: String): Boolean {
        val decoded = "decoded"+authenticationHelper.decrypt(token)
        print(authenticationHelper.decrypt(token))
        val data = JwtDecoder.decode(decoded)
        return try {
            val expClaim = data["exp"]?.jsonPrimitive?.longOrNull
            val isOfflineClaim = data["isOffLine"]?.jsonPrimitive?.booleanOrNull

            if (expClaim != null) {
                // For old token expiry check
                expClaim < Clock.System.now().toEpochMilliseconds() / 1000 // JWT exp is usually in seconds
            } else {
                // For new token expiry check
                isOfflineClaim?.not() ?: true
            }
        } catch (e: Exception) {
            data["isOffLine"]?.jsonPrimitive?.booleanOrNull?.not() ?: true
        }
    }

    private fun getListOfTestFromAuth(token: String): List<EcgTestType> {
        val data = JwtDecoder.decode(authenticationHelper.decrypt(token))
        val ecgTestTypeList = arrayListOf<EcgTestType>()

        val taClaim = data["ta"]
        val taString = taClaim?.jsonPrimitive?.contentOrNull
        taString?.split(",")?.forEach {
            when {
                it.contains(EcgTestType.LEAD_TWO.name) -> ecgTestTypeList.add(EcgTestType.LEAD_TWO)
                it.contains(EcgTestType.TWELVE_LEAD.name) -> ecgTestTypeList.add(EcgTestType.TWELVE_LEAD)
                it.contains(EcgTestType.HYPERKALEMIA.name) -> ecgTestTypeList.add(EcgTestType.HYPERKALEMIA)
                else -> ecgTestTypeList.add(EcgTestType.HRV)
            }
        }

        return ecgTestTypeList
    }





    private fun getEnabledDeviceTypeFromAuth(token: String): List<SpandanDeviceVariant> {
        val data = JwtDecoder.decode(authenticationHelper.decrypt(token))
        val deviceVariantList = arrayListOf<SpandanDeviceVariant>()

        val deClaim = data["de"]
        val deString = deClaim?.jsonPrimitive?.contentOrNull
        deString?.split(",")?.forEach {
            when {
                it.contains(SpandanDeviceVariant.SPANDAN_NEO.name) -> deviceVariantList.add(SpandanDeviceVariant.SPANDAN_NEO)
                it.contains(SpandanDeviceVariant.SPANDAN_PRO.name) -> deviceVariantList.add(SpandanDeviceVariant.SPANDAN_PRO)
                else -> deviceVariantList.add(SpandanDeviceVariant.SPANDAN_LEGACY)
            }
        }

        return deviceVariantList
    }




    private fun isGenerateReport(token: String): Boolean {
        // This method is called to check if the client has access to generate the report.
        val data = JwtDecoder.decode(authenticationHelper.decrypt(token))
        return data["eG"]?.jsonPrimitive?.contentOrNull?.toBoolean() ?: false
    }



    private fun createSessionId(): String {
        val allowedCharacters = "0123456789qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM"
        val sizeOfRandomString = 64
//        val random = Random()
        val sessionId = StringBuilder(sizeOfRandomString)
        for (i in 0 until sizeOfRandomString)
            sessionId.append(allowedCharacters[Random.nextInt(allowedCharacters.length)])
        return sessionId.toString()
    }

    fun encodeToBase64(input: ArrayList<Double>?): String {

        val joinedValues = input!!.joinToString(separator = ", ")

        val encoded = joinedValues.base64UrlEncoded // "Hello, world!"


        return encoded
    }
}