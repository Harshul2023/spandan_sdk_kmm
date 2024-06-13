package com.example.spandansdkkmm

import AuthenticationHelper
import DeviceInfo
import EcgTest
import GenerateReportModel
import MixPanelHelper
import PDFReportGenerationCallback
import com.example.spandansdkkmm.Const.CONNECTED_DEVICE_TYPE
import com.example.spandansdkkmm.Const.DEVICE_CONNECTION_TIMEOUT
import com.example.spandansdkkmm.Const.DEVICE_DISCONNECTED
import com.example.spandansdkkmm.Const.DEVICE_VERIFIED
import com.example.spandansdkkmm.Const.ERROR_TEST_NOT_VALID
import com.example.spandansdkkmm.Const.GENERATE_REPORT_CALLED
import com.example.spandansdkkmm.Const.GENERATE_REPORT_SUCCESS
import com.example.spandansdkkmm.Const.MASTER_KEY
import com.example.spandansdkkmm.Const.REASON
import com.example.spandansdkkmm.Const.SDK_INITIALISE_FAILED
import com.example.spandansdkkmm.Const.TEST_CREATED
import com.example.spandansdkkmm.Const.TEST_CREATE_FAILED
import com.example.spandansdkkmm.Const.TEST_TYPE
import com.example.spandansdkkmm.collection.EcgTestCallback
import com.example.spandansdkkmm.conclusion.EcgReport
import com.example.spandansdkkmm.connection.OnDeviceConnectionStateChangeListener
import com.example.spandansdkkmm.connection.SpandanSDKException
import com.example.spandansdkkmm.enums.EcgPosition
import com.example.spandansdkkmm.enums.EcgTestType
import com.example.spandansdkkmm.enums.SpandanDeviceVariant
import com.example.spandansdkkmm.retrofit_helper.ApiEcgData
import com.example.spandansdkkmm.retrofit_helper.GenerateAuthTokenResult
import com.example.spandansdkkmm.retrofit_helper.GeneratePdfReportInputData
import com.example.spandansdkkmm.retrofit_helper.MetaData
import com.example.spandansdkkmm.retrofit_helper.PatientData
import `in`.sunfox.healthcare.commons.android.spandan_sdk.enums.SpandanException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.internal.synchronized
import kotlin.concurrent.Volatile
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.jvm.JvmStatic


class SpandanSDK private constructor() {
    private var onDeviceConnectionStateChangeListener: OnDeviceConnectionStateChangeListener? = null
    private lateinit var report: EcgReport
    private lateinit var ecgTestType: EcgTestType
    private lateinit var ecgTestInstance: EcgTest
    private lateinit var authenticationHelper: AuthenticationHelper
    private var enabledEcgTests: List<EcgTestType>? = null
    private var enabledSpandanDeviceVariants: List<SpandanDeviceVariant> = arrayListOf()
    private lateinit var application: Application
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
        private lateinit var mixPanelHelper: MixPanelHelper

        @Volatile
        private lateinit var masterKey: String

        @Volatile
        private lateinit var verifierToken: String

        @Volatile
        private var generatedAuthToken: String? = null

        @JvmStatic
        fun getInstance(): SpandanSDK {
            return INSTANCE!!
        }

        @JvmStatic
        private fun getSessionId() = sessionId

        @JvmStatic
        fun initializeOffline(application: Application, token: String) {
            synchronized(this) {
                masterKey = token
                mixPanelHelper = MixPanelHelper.getInstance(application)
                if (INSTANCE == null) {
                    INSTANCE = SpandanSDK()
                    val w = String(Base64.decode(token, Base64.DEFAULT))
                    val id = w.substring(0, 64)
                    val createdAt = w.substring(65, 78)
                    val masterKey = w.substring(79, 95)
                    generatedAuthToken = w.substring(96, token.length)
                    INSTANCE!!.authenticationHelper =
                        AuthenticationHelper(id = id, createdAt = createdAt, masterKey = masterKey)
                    if (INSTANCE!!.validateOfflineAuthKey(token)) INSTANCE!!.bind(application)
                    else throw SpandanSDKException("${SpandanException.SDKNotInitialisedException.name}: Could not initialise Spandan SDK. The token is invalid. Please check the token and try again.")
                }
            }
        }


        @JvmStatic
        fun initialize(
            application: Application,
            verifierToken: String,
            masterKey: String,
            onInitializationCompleteListener: OnInitializationCompleteListener,
        ) {
            /**
             * @param organizationUniqueId can be used to uniquely identified the package name.**/
            synchronized(this) {
                mixPanelHelper = MixPanelHelper.getInstance(application)
                this.masterKey = masterKey
                this.verifierToken = verifierToken
                if (INSTANCE == null) {
                    INSTANCE = SpandanSDK()
                    INSTANCE!!.bind(application)
                    CoroutineScope(IO).launch {
                        sessionId = INSTANCE!!.createSessionId()
                        RetrofitHelper()
                            .getRetrofitInstance()
                            .getAuthToken(
                                authorization = verifierToken,
                                apiKey = masterKey,
                                sessionId = sessionId!!
                            )
                            .enqueue(object : Callback<GenerateAuthTokenResult> {
                                override fun onResponse(
                                    call: Call<GenerateAuthTokenResult>,
                                    response: Response<GenerateAuthTokenResult>,
                                ) {
                                    var errorMsg = StringBuffer("")
                                    if (!response.isSuccessful) {
                                        val errorGson = Gson().fromJson(
                                            Gson().toJson((response.errorBody() as ResponseBody).string()),
                                            ErrorResponse::class.java
                                        )
                                        errorMsg = when (response.code()) {
                                            401 -> StringBuffer(errorGson.message)
                                            else -> StringBuffer("Internal server error. Please contact Sunfox support team")
                                        }
                                        mixPanelHelper.sendToMixpanel(
                                            eventName = SDK_INITIALISE_FAILED,
                                            key = arrayListOf(
                                                MASTER_KEY,
                                                REASON
                                            ),
                                            value = arrayListOf(
                                                masterKey,
                                                errorMsg.toString()
                                            )
                                        )
                                        onInitializationCompleteListener.onInitializationFailed(
                                            errorMsg.toString()
                                        )
                                    }
                                    response.body().let {
                                        if (it != null) {
                                            if (!it.success) {
                                                mixPanelHelper.sendToMixpanel(
                                                    eventName = SDK_INITIALISE_FAILED,
                                                    key = arrayListOf(
                                                        MASTER_KEY,
                                                        REASON
                                                    ),
                                                    value = arrayListOf(
                                                        masterKey,
                                                        it.message
                                                    )
                                                )
                                                onInitializationCompleteListener.onInitializationFailed(
                                                    it.message
                                                )
                                            } else {
                                                mixPanelHelper.sendToMixpanel(
                                                    eventName = SDK_INITIALISE_COMPLETE,
                                                    key = arrayListOf(MASTER_KEY),
                                                    value = arrayListOf(masterKey)
                                                )
                                                generatedAuthToken = it.token
                                                INSTANCE!!.authenticationHelper =
                                                    AuthenticationHelper(
                                                        it.id,
                                                        it.createdAt,
                                                        masterKey
                                                    ) //please provide inputKey as combination of id+created_at+master_key

//                                                INSTANCE!!.authenticationHelper.initialiseDecryptionKey(
//                                                    it.id + it.createdAt + masterKey,
//                                                    it.id.substring(0, 16)
//                                                )

                                                onInitializationCompleteListener.onInitializationSuccess()
                                                try {
                                                    INSTANCE!!.decodeDeviceVariantAndTestType(
                                                        generatedAuthToken!!
                                                    )
                                                } catch (e: Exception) {
                                                    /**
                                                     * This catch block executed if jwt token can't be decoded.*/
                                                    onInitializationCompleteListener.onInitializationFailed(
                                                        "${SpandanException.InvalidSessionException.name}: The session is not valid. Please re-initialise the Spandan SDK. Error Code: 4"
                                                    )
                                                }
                                            }
                                        } else {
                                            mixPanelHelper.sendToMixpanel(
                                                eventName = SDK_INITIALISE_FAILED,
                                                key = arrayListOf(MASTER_KEY, REASON),
                                                value = arrayListOf(
                                                    masterKey,
                                                    "Internal server error. Please contact Sunfox support team"
                                                )
                                            )
                                            onInitializationCompleteListener.onInitializationFailed(
                                                "Internal server error. Please contact Sunfox support team"
                                            )
                                        }
                                    }
                                }

                                override fun onFailure(
                                    call: Call<GenerateAuthTokenResult>,
                                    t: Throwable,
                                ) {
                                    Log.d("Spandan.TAG", "onFailure: ${t.toString()}")
                                    mixPanelHelper.sendToMixpanel(
                                        eventName = SDK_INITIALISE_FAILED,
                                        key = arrayListOf(
                                            MASTER_KEY,
                                            REASON
                                        ),
                                        value = arrayListOf(
                                            masterKey,
                                            if (t.message != null) "Initialization failed: ${t.message}"
                                            else "Initialisation failed. Please contact Sunfox support team"
                                        )
                                    )
                                    onInitializationCompleteListener.onInitializationFailed(
                                        if (t.message != null) "Initialization failed: ${t.message}"
                                        else "Initialisation failed. Please contact Sunfox support team"
                                    )
                                }
                            })
                    }
                }
            }
        }

        @Volatile
        private var isDeviceConnected = false
    }


    fun setOnDeviceConnectionStateChangedListener(onDeviceConnectionStateChangeListener: OnDeviceConnectionStateChangeListener) {
        this.onDeviceConnectionStateChangeListener = onDeviceConnectionStateChangeListener
    }

    fun bind(application: Application) {
        SeriCom.initialize(application)
        this.application = application
        if (!SeriCom.isDeviceConnected()) {
            isDeviceConnected = true
        }

        SeriCom.setOnConnectionChangeListener(object : OnConnectionStateChangeListener {
            override fun onConnectionError(errorCode: DeviceErrorState, errorMessage: String?) {
                when (errorCode) {
                    DeviceErrorState.CONNECTION -> {}
                    DeviceErrorState.PERMISSION_DENIED -> {
                        onDeviceConnectionStateChangeListener?.onUsbPermissionDenied()
                    }

                    DeviceErrorState.ENDPOINT -> {}
                    DeviceErrorState.USB_REQUEST -> {}
                }
            }

            override fun onDeviceAttached() {
                isDeviceConnected = true
                onDeviceConnectionStateChangeListener?.onDeviceAttached()
            }

            override fun onDeviceConnected() {
                mixPanelHelper.sendToMixpanel(
                    eventName = DEVICE_CONNECTED,
                    key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE),
                    value = arrayListOf(
                        masterKey,
                        if (getDeviceVariantString() != "") getDeviceVariantString() else {
                            "device not verified yet."
                        }
                    )
                )
                sendCommand = "c"
                SeriCom.sendCommand(sendCommand)
                val handler1 = Handler(Looper.getMainLooper())
                handler1.postDelayed({
                    if (!isDeviceVerified) {
                        mixPanelHelper.sendToMixpanel(
                            eventName = DEVICE_CONNECTION_TIMEOUT,
                            key = arrayListOf(
                                MASTER_KEY
//                                , CONNECTED_DEVICE_TYPE
                            ),
                            value = arrayListOf(
                                masterKey,
//                                if (getVariant() != "") getVariant() else {
//                                    "device not verified yet."
//                                }
                            )
                        )
                        onDeviceConnectionStateChangeListener?.onConnectionTimedOut()
                    }
                }, 5000)
            }

            override fun onDeviceDisconnected() {
                val deviceVariant = getDeviceVariantString()
                mixPanelHelper.sendToMixpanel(
                    eventName = DEVICE_DISCONNECTED,
                    key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE),
                    value = arrayListOf(
                        masterKey,
                        if (deviceVariant != "") deviceVariant else {
                            "device not verified yet."
                        }
                    )
                )
                isDeviceConnected = false
                isDeviceVerified = false

                onDeviceConnectionStateChangeListener?.onDeviceDisconnected()
                if (::ecgTestInstance.isInitialized)
                    ecgTestInstance.onDeviceDisconnected()
            }

            override fun onReceivedData(data: String) {
                populateDeviceInfo(data)
            }

        })
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
                    mixPanelHelper.sendToMixpanel(
                        eventName = TEST_CREATE_FAILED,
                        key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE, REASON),
                        value = arrayListOf(
                            masterKey,
                            getDeviceVariantString(),
                            ecgTestType.name,
                            exception.toString()
                        )
                    )
                    throw exception
                }
            } else {
                if (!validateAuthKey(generatedAuthToken!!)) {
                    val exception =
                        SpandanSDKException("${SpandanException.InvalidSessionException.name}: The session is invalid. Please re-initialize the Spandan SDK")
                    mixPanelHelper.sendToMixpanel(
                        eventName = TEST_CREATE_FAILED,
                        key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE, REASON),
                        value = arrayListOf(
                            masterKey,
                            getDeviceVariantString(),
                            ecgTestType.name,
                            exception.toString()
                        )
                    )
                    throw exception
                }
            }
        }
        mixPanelHelper.sendToMixpanel(
            eventName = TEST_CREATED,
            key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE),
            value = arrayListOf(masterKey, getDeviceVariantString(), ecgTestType.name)
        )
        enabledEcgTests.let {
            if (it != null) {
                if (!it.contains(EcgTestType.HRV) && ecgTestType == EcgTestType.HRV) {
                    val exception =
                        SpandanSDKException("${SpandanException.InvalidTestException.name}: Not authorised for this test. Please contact Sunfox support.")
                    mixPanelHelper.sendToMixpanel(
                        eventName = TEST_CREATE_FAILED,
                        key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE, REASON),
                        value = arrayListOf(
                            masterKey,
                            getDeviceVariantString(),
                            ecgTestType.name,
                            exception.toString()
                        )
                    )
                    throw exception
                }
                if (!it.contains(EcgTestType.LEAD_TWO) && ecgTestType == EcgTestType.LEAD_TWO) {
                    val exception =
                        SpandanSDKException("${SpandanException.InvalidTestException.name}: Not authorised for this test. Please contact Sunfox support.")
                    mixPanelHelper.sendToMixpanel(
                        eventName = TEST_CREATE_FAILED,
                        key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE, REASON),
                        value = arrayListOf(
                            masterKey,
                            getDeviceVariantString(),
                            ecgTestType.name,
                            exception.toString()
                        )
                    )
                    throw exception
                }
                if (!it.contains(EcgTestType.TWELVE_LEAD) && ecgTestType == EcgTestType.TWELVE_LEAD) {
                    val exception =
                        SpandanSDKException("${SpandanException.InvalidTestException.name}: Not authorised for this test. Please contact Sunfox support.")
                    mixPanelHelper.sendToMixpanel(
                        eventName = TEST_CREATE_FAILED,
                        key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE, REASON),
                        value = arrayListOf(
                            masterKey,
                            getDeviceVariantString(),
                            ecgTestType.name,
                            exception.toString()
                        )
                    )
                    throw exception
                }
                if (!it.contains(EcgTestType.HYPERKALEMIA) && ecgTestType == EcgTestType.HYPERKALEMIA) {
                    val exception =
                        SpandanSDKException("${SpandanException.InvalidTestException.name}: Not authorised for this test. Please contact Sunfox support.")
                    mixPanelHelper.sendToMixpanel(
                        eventName = TEST_CREATE_FAILED,
                        key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE, REASON),
                        value = arrayListOf(
                            masterKey,
                            getDeviceVariantString(),
                            ecgTestType.name,
                            exception.toString()
                        )
                    )
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
        return SeriCom.isDeviceConnected()
    }

    fun unbind() {
        deviceInfo.let {
            if (it != null)
                SeriCom.sendCommand(if (it.deviceVariant == SpandanDeviceVariant.SPANDAN_LEGACY) "0" else if (it.deviceVariant == SpandanDeviceVariant.SPANDAN_NEO) "STP" else "0")
            else
                throw SpandanSDKException(SpandanException.DeviceNotConnectedException.name + ": Spandan device not connected. Please connect the device")
        }
        SeriCom.clearInstance()
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
                mixPanelHelper.sendToMixpanel(
                    eventName = Const.GENERATE_REPORT_FAILED,
                    key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE, REASON),
                    value = arrayListOf(
                        masterKey,
                        getDeviceVariantString(),
                        ecgTestType.name,
                        exception.toString()
                    )
                )
                throw exception
            }
        } else {
            if (!validateAuthKey(generatedAuthToken!!)) {
                val exception =
                    SpandanSDKException("${SpandanException.InvalidSessionException.name}: The session is invalid. Please re-initialize the Spandan SDK")
                mixPanelHelper.sendToMixpanel(
                    eventName = Const.GENERATE_REPORT_FAILED,
                    key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE, REASON),
                    value = arrayListOf(
                        masterKey,
                        getDeviceVariantString(),
                        ecgTestType.name,
                        exception.toString()
                    )
                )
                throw exception
            }
        }
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            val error = StringBuilder()
            error.append(throwable.message)
            throwable.stackTrace.forEach {
                error.append("$it \n")
            }
            mixPanelHelper.sendToMixpanel(
                eventName = Const.GENERATE_REPORT_FAILED,
                key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE, REASON),
                value = arrayListOf(
                    masterKey,
                    getDeviceVariantString(),
                    ecgTestType.name,
                    error.toString()
                )
            )
            reportGenerationStatusListener.onReportGenerationFailed(
                ERROR_TEST_NOT_VALID,
                "$error"
            )
        }

        mixPanelHelper.sendTimingEvent(
            eventName = GENERATE_REPORT_CALLED,
            key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE),
            value = arrayListOf(masterKey, getDeviceVariantString(), ecgTestType.name),
            true
        )
        CoroutineScope(IO).launch(exceptionHandler) {
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
            mixPanelHelper.sendTimingEvent(
                eventName = GENERATE_REPORT_CALLED,
                key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE),
                value = arrayListOf(masterKey, getDeviceVariantString(), ecgTestType.name),
                false
            )
            mixPanelHelper.sendToMixpanel(
                eventName = GENERATE_REPORT_SUCCESS,
                key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE),
                value = arrayListOf(masterKey, getDeviceVariantString(), ecgTestType.name)
            )
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
                        if (dataArray[2].substring(4) == masterKey) {
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
                            mixPanelHelper.sendToMixpanel(
                                eventName = DEVICE_VERIFIED,
                                key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE),
                                value = arrayListOf(masterKey, getDeviceVariantString())
                            )
                            if (isDeviceVerified) onDeviceConnectionStateChangeListener?.onDeviceConnected(
                                deviceInfo!!
                            )
                        }
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
                        mixPanelHelper.sendToMixpanel(
                            eventName = DEVICE_VERIFIED,
                            key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE),
                            value = arrayListOf(masterKey, getDeviceVariantString())
                        )
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
                mixPanelHelper.sendToMixpanel(
                    eventName = DEVICE_VERIFIED,
                    key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE),
                    value = arrayListOf(masterKey, getDeviceVariantString())
                )
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
                    v1Data =
                    Base64.encodeToString(
                        ecgTest._ecgData[EcgPosition.V1].toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT
                    ),

                    v2Data =
                    Base64.encodeToString(
                        ecgTest._ecgData[EcgPosition.V2].toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT
                    ),

                    v3Data =
                    Base64.encodeToString(
                        ecgTest._ecgData[EcgPosition.V3].toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT
                    ),

                    v4Data =
                    Base64.encodeToString(
                        ecgTest._ecgData[EcgPosition.V4].toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT
                    ),

                    v5Data =
                    Base64.encodeToString(
                        ecgTest._ecgData[EcgPosition.V5].toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT
                    ),

                    v6Data =
                    Base64.encodeToString(
                        ecgTest._ecgData[EcgPosition.V6].toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT
                    ),

                    lead1Data =
                    Base64.encodeToString(
                        ecgTest._ecgData[EcgPosition.LEAD_1].toString().toByteArray(Charsets.UTF_8),
                        Base64.DEFAULT
                    ),

                    lead2Data =
                    Base64.encodeToString(
                        ecgTest._ecgData[EcgPosition.LEAD_2].toString().toByteArray(Charsets.UTF_8),
                        Base64.DEFAULT
                    )
                )

            }

            (EcgTestType.HRV) -> {}

            (EcgTestType.LEAD_TWO) -> {
                apiEcgData = ApiEcgData(
                    lead2Data =
                    Base64.encodeToString(
                        ecgTest._ecgData[LEAD_2].toString().toByteArray(Charsets.UTF_8),
                        Base64.DEFAULT
                    )
                )
            }

            (EcgTestType.HYPERKALEMIA) -> {

                apiEcgData = ApiEcgData(
                    v1Data =
                    Base64.encodeToString(
                        ecgTest._ecgData[V1].toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT
                    ),

                    v2Data =
                    Base64.encodeToString(
                        ecgTest._ecgData[V2].toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT
                    ),

                    v3Data =
                    Base64.encodeToString(
                        ecgTest._ecgData[V3].toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT
                    ),

                    v4Data =
                    Base64.encodeToString(
                        ecgTest._ecgData[V4].toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT
                    ),

                    v5Data =
                    Base64.encodeToString(
                        ecgTest._ecgData[V5].toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT
                    ),

                    v6Data =
                    Base64.encodeToString(
                        ecgTest._ecgData[V6].toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT
                    ),

                    lead2Data =
                    Base64.encodeToString(
                        ecgTest._ecgData[LEAD_2].toString().toByteArray(Charsets.UTF_8),
                        Base64.DEFAULT
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
                mixPanelHelper.sendToMixpanel(
                    eventName = Const.GENERATE_REPORT_FAILED,
                    key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE, REASON),
                    value = arrayListOf(
                        masterKey,
                        getDeviceVariantString(),
                        ecgTestType.name,
                        exception.toString()
                    )
                )
                throw exception
            }
        } else {
            mixPanelHelper.sendTimingEvent(
                eventName = GENERATE_REPORT_CALLED,
                key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE),
                value = arrayListOf(masterKey, getDeviceVariantString(), ecgTestType.name),
                true
            )

            RetrofitHelper()
                .getRetrofitInstance()
                .generatePdfReport(
                    authorization = verifierToken,
                    apiKey = masterKey,
                    generatePdfReportInputData = inputForProcessApi
                )
                .enqueue(object : Callback<GeneratePdfReportResult> {

                    override fun onResponse(
                        call: Call<GeneratePdfReportResult>,
                        response: Response<GeneratePdfReportResult>,
                    ) {
                        var errorMsg = StringBuffer("")
                        if (!response.isSuccessful) {
                            val errorBody = Gson().fromJson(
                                Gson().toJson((response.errorBody() as ResponseBody).string()),
                                ErrorResponse::class.java
                            )
                            errorMsg = when (response.code()) {
                                400 -> StringBuffer(errorBody.message)
                                401 -> StringBuffer(errorBody.message) //authorization
                                else -> StringBuffer("Internal server error. Please contact Sunfox support team")
                            }
                            errorMsg.append("If the error occurs repeatedly, please contact Sunfox support.")
                            mixPanelHelper.sendToMixpanel(
                                eventName = Const.GENERATE_REPORT_FAILED,
                                key = arrayListOf(
                                    MASTER_KEY,
                                    CONNECTED_DEVICE_TYPE,
                                    TEST_TYPE,
                                    REASON
                                ),
                                value = arrayListOf(
                                    masterKey,
                                    getDeviceVariantString(),
                                    ecgTestType.name,
                                    errorMsg.toString()
                                )
                            )
                            pdfReportGenerationCallback.onReportGenerationFailed(errorMsg.toString())
                        }

                        response.body().let { ecgApiResult ->
                            if (ecgApiResult != null && ecgApiResult.success) {
                                pdfReportGenerationCallback.onReportGenerationSuccess(
                                    ecgApiResult.data
                                )
                            } else {
                                mixPanelHelper.sendToMixpanel(
                                    eventName = Const.GENERATE_REPORT_FAILED,
                                    key = arrayListOf(
                                        MASTER_KEY,
                                        CONNECTED_DEVICE_TYPE,
                                        TEST_TYPE,
                                        REASON
                                    ),
                                    value = arrayListOf(
                                        masterKey,
                                        getDeviceVariantString(),
                                        ecgTestType.name,
                                        ecgApiResult?.message ?: ""
                                    )
                                )
                                pdfReportGenerationCallback.onReportGenerationFailed(
                                    ecgApiResult?.message ?: response.message()
                                )
                            }
                        }
                    }

                    override fun onFailure(call: Call<GeneratePdfReportResult>, t: Throwable) {
                        pdfReportGenerationCallback.onReportGenerationFailed(t.stackTraceToString())
                    }
                })
        }
    }


    private fun isTokenExpired(token: String, sessionId: String?): Boolean {
        val data = JWT(authenticationHelper.decrypt(token))
        return !(data.claims["sid"]!!.asString().equals(sessionId, false))
    }

    private fun isOfflineTokenExpired(token: String): Boolean {
        val data = JWT(authenticationHelper.decrypt(token))
        return try {
            if (data.claims["exp"] != null) {
                //for old token expiry check
                (data.claims["exp"]!!.asLong()!! < (System.currentTimeMillis()))
            } else {
                //for new token expiry check
                !data.claims["isOffLine"]!!.asBoolean()!!
            }
        } catch (e: Exception) {
            !data.claims["isOffLine"]!!.asBoolean()!!
        }
    }

    private fun getListOfTestFromAuth(token: String): List<EcgTestType> {
        val data = JWT(authenticationHelper.decrypt(token))
        val ecgTestTypeList = arrayListOf<EcgTestType>()
        (data.claims["ta"])!!.asString()!!.split(Regex(",")).forEach {
            if (it.contains(EcgTestType.LEAD_TWO.name))
                ecgTestTypeList.add(EcgTestType.LEAD_TWO)
            else if (it.contains(EcgTestType.TWELVE_LEAD.name))
                ecgTestTypeList.add(EcgTestType.TWELVE_LEAD)
            else if (it.contains(EcgTestType.HYPERKALEMIA.name))
                ecgTestTypeList.add(EcgTestType.HYPERKALEMIA)
            else
                ecgTestTypeList.add(EcgTestType.HRV)
        }
        return ecgTestTypeList
    }

    private fun getEnabledDeviceTypeFromAuth(token: String): List<SpandanDeviceVariant> {
        val data = JWT(authenticationHelper.decrypt(token))
        val deviceVariantList = arrayListOf<SpandanDeviceVariant>()
        (data.claims["de"])!!.asString()!!.split(Regex(",")).forEach {
            if (it.contains(SpandanDeviceVariant.SPANDAN_NEO.name))
                deviceVariantList.add(SpandanDeviceVariant.SPANDAN_NEO)
            else if (it.contains(SpandanDeviceVariant.SPANDAN_PRO.name))
                deviceVariantList.add(SpandanDeviceVariant.SPANDAN_PRO)
            else
                deviceVariantList.add(SpandanDeviceVariant.SPANDAN_LEGACY)
        }
        return deviceVariantList//for checking enabled device type(Neo, Pro or Legacy) from device
    }

    private fun isUiEnabled(token: String): Boolean {
        val data = JWT(authenticationHelper.decrypt(token))
        return data.claims["eUi"].let {
            if (it != null)
                it.asBoolean()!!
            else
                false
        }
    }

    private fun isGenerateReport(token: String): Boolean {
        //this method is called to check if the client have access to the generate the report.
        val data = JWT(authenticationHelper.decrypt(token))
        return data.claims["eG"].let {
            if (it != null)
                it.asBoolean()!!
            else
                false
        }
    }

    private fun createSessionId(): String {
        val allowedCharacters = "0123456789qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM"
        val sizeOfRandomString = 64
        val random = Random()
        val sessionId = StringBuilder(sizeOfRandomString)
        for (i in 0 until sizeOfRandomString)
            sessionId.append(allowedCharacters[random.nextInt(allowedCharacters.length)])
        return sessionId.toString()
    }
}