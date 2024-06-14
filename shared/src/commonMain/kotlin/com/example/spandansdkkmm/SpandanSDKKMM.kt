//// SpandanSDK.kt
//package com.example.spandansdkkmm
//
//import com.example.spandansdkkmm.listener.ConnectionStateListener
//import com.example.spandansdkkmm.connection.OnDeviceConnectionStateChangeListener
//import com.example.spandansdkkmm.enums.DeviceErrorState
//import com.example.spandansdkkmm.enums.EcgTestType
//import com.example.spandansdkkmm.enums.SpandanDeviceVariant
//
//import `in`.sunfox.healthcare.commons.android.spandan_sdk.connection.DeviceInfo
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.InternalCoroutinesApi
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.internal.SynchronizedObject
//import kotlinx.coroutines.launch
//
//import kotlin.concurrent.Volatile
//
//class SpandanSDKKMM {
//    private val platform: Platform = getPlatform()
//    private var application: Any? = null;
//    private var isDeviceConnected: Boolean = false;
//    private var useOfflineToken = false
//    private var isDeviceVerified = false
//    private var deviceInfo: DeviceInfo? = null
//    private var sendCommand = ""
//    private var isVerificationProcessDone = false
//    private var onDeviceConnectionStateChangeListener: OnDeviceConnectionStateChangeListener? = null
//    private lateinit var ecgTestType: EcgTestType
////    private lateinit var ecgTestInstance: EcgTest
//
//    private var enabledEcgTests: List<EcgTestType>? = null
//    private var enabledSpandanDeviceVariants: List<SpandanDeviceVariant> = arrayListOf()
//    private val scope = CoroutineScope(Dispatchers.Main)
//    private val TAG = "SpandanSDK.TAG"
//
////    @OptIn(InternalCoroutinesApi::class)
//
//    companion object{
//        @Volatile
//        private var INSTANCE: SpandanSDKKMM? = null
//
//        @Volatile
//        private var sessionId: String? = null
//
////        @Volatile
////        private lateinit var mixPanelHelper: MixPanelHelper
//
////        @Volatile
////        private var enabledSpandanDeviceVariants: List<SpandanDeviceVariant> = arrayListOf()
//
////        @Volatile
////        private lateinit var masterKey: String
//
//        @Volatile
//        private lateinit var verifierToken: String
//
//        @OptIn(InternalCoroutinesApi::class)
//        private val lock = SynchronizedObject()
//        @Volatile
//        private var generatedToken: String? = null
//
//        fun getInstance(): SpandanSDKKMM {
//            if(INSTANCE == null) INSTANCE = SpandanSDKKMM()
//            return INSTANCE!!
//        }
//
//        private fun getSessionId() = sessionId
//
////        @JvmStatic
////        private fun getDecodedEnabledTest() = enabledSpandanDeviceVariants
//
////        @OptIn(InternalCoroutinesApi::class)
////       @OptIn(InternalCoroutinesApi::class)
////       fun initializeOffline(application: Any, token: String) {
////            synchronized(this) {
////                masterKey = token
////                mixPanelHelper = MixPanelHelper.getInstance(application)
////                if (INSTANCE == null) {
////                    INSTANCE = SpandanSDK()
////                    if (INSTANCE!!.validateOfflineAuthKey(token)) INSTANCE!!.bind(application)
////                    else throw SpandanSDKException("${SpandanException.AuthenticationException}: The session is invalid. Please re-initialize the Spandan SDK")
////                }
////            }
////        }
//
////        fun initialize(
////            application: Any,
////            verifierToken: String,
////            masterKey: String,
////            onInitializationCompleteListener: OnInitializationCompleteListener,
////        ) {
////            /**
////             * @param organizationUniqueId can be used to uniquely identified the package name.**/
////            val organizationUniqueId = application.applicationInfo.packageName
////            synchronized(this) {
////                mixPanelHelper = MixPanelHelper.getInstance(application)
////                this.masterKey = masterKey
////                this.verifierToken = verifierToken
////                if (INSTANCE == null) {
////                    INSTANCE = SpandanSDK()
////                    INSTANCE!!.bind(application)
////                    CoroutineScope(IO).launch {
////                        sessionId = AuthenticationHelper.createSessionId()
////                        RetrofitHelper()
////                            .getRetrofitInstance()
////                            .getToken(
////                                verifierToken = verifierToken,
////                                apiKey = masterKey,
////                                sessionId = sessionId!!
////                            )
////                            .enqueue(object : Callback<TokenRefreshResult> {
////                                override fun onResponse(
////                                    call: Call<TokenRefreshResult>,
////                                    response: Response<TokenRefreshResult>,
////                                ) {
////                                    response.body().let {
////                                        if (it != null) {
////                                            if (!it.success) {
////                                                mixPanelHelper.sendToMixpanel(
////                                                    eventName = SDK_INITIALISE_FAILED,
////                                                    key = arrayListOf(
////                                                        MASTER_KEY,
////                                                        REASON
////                                                    ),
////                                                    value = arrayListOf(
////                                                        masterKey,
////                                                        it.message
////                                                    )
////                                                )
////                                                onInitializationCompleteListener.onInitializationFailed(
////                                                    it.message
////                                                )
////                                            } else {
////                                                mixPanelHelper.sendToMixpanel(
////                                                    eventName = SDK_INITIALISE_COMPLETE,
////                                                    key = arrayListOf(
////                                                        MASTER_KEY
////                                                    ),
////                                                    value = arrayListOf(
////                                                        masterKey
////                                                    )
////                                                )
//////                                                enabledSpandanDeviceVariants =
//////                                                    AuthenticationHelper.getEnabledDeviceTypeFromAuth(
//////                                                        it.token
//////                                                    )
////                                                generatedToken = it.token
////                                                onInitializationCompleteListener.onInitializationSuccess()
////                                                INSTANCE!!.decodeDeviceVariantAndTestType(generatedToken!!)
////                                            }
////                                        } else {
////                                            mixPanelHelper.sendToMixpanel(
////                                                eventName = SDK_INITIALISE_FAILED,
////                                                key = arrayListOf(
////                                                    MASTER_KEY,
////                                                    REASON
////                                                ),
////                                                value = arrayListOf(
////                                                    masterKey,
////                                                    "Internal server error. Please contact Sunfox support team"
////                                                )
////                                            )
////                                            onInitializationCompleteListener.onInitializationFailed(
////                                                "Internal server error. Please contact Sunfox support team"
////                                            )
////                                        }
////                                    }
////                                }
////
////                                override fun onFailure(
////                                    call: Call<TokenRefreshResult>,
////                                    t: Throwable,
////                                ) {
////                                    mixPanelHelper.sendToMixpanel(
////                                        eventName = SDK_INITIALISE_FAILED,
////                                        key = arrayListOf(
////                                            MASTER_KEY,
////                                            REASON
////                                        ),
////                                        value = arrayListOf(
////                                            masterKey,
////                                            if(t.message!=null) "Initialization failed: $t.message"
////                                            else "Initialisation failed. Please contact Sunfox support team"
////                                        )
////                                    )
////                                    onInitializationCompleteListener.onInitializationFailed(
////                                        if(t.message!=null) "Initialization failed: $t.message"
////                                        else "Initialisation failed. Please contact Sunfox support team"
////                                    )
////                                }
////                            })
////                    }
////                }
////            }
////        }
//
//        @Volatile
//        private var isDeviceConnected = false
//    }
//
//    fun greet(): String {
//        return "Hello, ${platform.name}!"
//
//    }
//
//    fun initializeAndroid(context: Any) {
//        getInitializer().initialize(context)
//    }
//
//    fun initializeIOS(context: Any) {
//        getInitializer().initialize(context)
//    }
//
//    fun initialize(context: Any) {
//        bind(context)
//
//    }
//
//    fun isDeviceConnected(): Boolean {
//        return getCommunicator().getDeviceConnected()
//    }
//
//    fun sendCommand(command: String) {
//        getCommunicator().sendCommand(command)
//        if (!isDeviceConnected()) {
//            isDeviceConnected = true
//        }
//    }
//    fun convertHexToAsciiAndKeepHex(hex: String): String {
//        val parts = hex.split("2d") // Split the hex string at each occurrence of "2D"
//        val result = StringBuilder()
//        for (i in parts.indices) {
//            if (i < parts.size - 1) {
//                // Convert each part to ASCII text except the last one
//                result.append(parts[i].chunked(2).map { it.toInt(16).toChar() }.joinToString(""))
//                result.append("-")
//            } else {
//                // Keep the last part as hex
//                result.append(parts[i])
//            }
//        }
//        return result.toString()
//    }
//
//    fun setOnDeviceConnectionStateChangedListener(onDeviceConnectionStateChangeListener: OnDeviceConnectionStateChangeListener) {
//        this.onDeviceConnectionStateChangeListener = onDeviceConnectionStateChangeListener
//    }
//    fun bind(application: Any) {
//        getInitializer().initialize(application)
//        this.application = application
//        if (isDeviceConnected()) {
//            isDeviceConnected = true
//        }
//        val connectionStateListener = object :ConnectionStateListener{
//            override fun onConnectionError(errorCode: DeviceErrorState) {
//                onDeviceConnectionStateChangeListener!!.onUsbPermissionDenied()
//            }
//
//            override fun onDeviceAttached() {
//                onDeviceConnectionStateChangeListener!!.onDeviceAttached()
//
//            }
//
//            override fun onDeviceConnected() {
//                scope.launch {
//                    delay(1000) // 2000 milliseconds delay
//                    getCommunicator().sendCommand("c")
//                   sendCommand = "c"
//                }//
//
//            }
//
//            override fun onDeviceDisconnected() {
//                print("DISCONNECTED")
//                onDeviceConnectionStateChangeListener!!.onDeviceDisconnected()
//            }
//
//            override fun onReceivedData(data: String) {
//
//                if(sendCommand =="c" && platform.name.contains("iOS"))
//                    populateDeviceInfo(convertHexToAsciiAndKeepHex(data))
//                else
//                    populateDeviceInfo(data)
//
//            }
//
//        }
//        setListener().setOnConnectionStateListener(connectionStateListener=connectionStateListener)
////        setListener().setOnConnectionStateListener(connectionStateListener = object :ConnectionStateListener{
////
////
////            override fun onConnectionError(errorCode: DeviceErrorState) {
////                when (errorCode) {
////                    DeviceErrorState.CONNECTION -> {}
////                    DeviceErrorState.BLUETOOTHNOTPOWEREDON -> {
////                        onDeviceConnectionStateChangeListener?.onUsbPermissionDenied()
////                    }
////
////                    DeviceErrorState.ENDPOINT -> {}
////                    DeviceErrorState.PERMISSIONDENIED -> {}
////                    DeviceErrorState.USB_REQUEST -> {}
////                }
////            }
////
////            override fun onDeviceAttached() {
////
////
////                print("DEVICE ATTACHED FROM KOTLIN")
////                isDeviceConnected = true
//////                onDeviceConnectionStateChangeListener?.onDeviceConnectionStateChanged(
//////                    DeviceConnectionState.CONNECTED
//////                )
////                onDeviceConnectionStateChangeListener?.onDeviceAttached()
////            }
////
////            override fun onDeviceConnected() {
////                print("DEvice Connected from kotlin")
////                getCommunicator().sendCommand("c")
////                sendCommand = "c";
//////                val handler1 = Handler(Looper.getMainLooper())
//////                handler1.postDelayed({
//////                    if (!isDeviceVerified) {
//////                        mixPanelHelper.sendToMixpanel(
//////                            eventName = DEVICE_CONNECTION_TIMEOUT,
//////                            key = arrayListOf(
//////                                MASTER_KEY
////////                                , CONNECTED_DEVICE_TYPE
//////                            ),
//////                            value = arrayListOf(
//////                                masterKey,
////////                                if (getVariant() != "") getVariant() else {
////////                                    "device not verified yet."
////////                                }
//////                            )
//////                        )
//////                        onDeviceConnectionStateChangeListener?.onConnectionTimedOut()
//////                    }
//////                }, 5000)
////            }
////
////            override fun onDeviceDisconnected() {
////                print("DEVICE DISCONNECTED FROM KOTLIN")
////                val deviceVariant = getDeviceVariantString()
//////                mixPanelHelper.sendToMixpanel(
//////                    eventName = DEVICE_DISCONNECTED,
//////                    key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE),
//////                    value = arrayListOf(
//////                        masterKey,
//////                        if (deviceVariant != "") deviceVariant else {
//////                            "device not verified yet."
//////                        }
//////                    )
//////                )
////                isDeviceConnected = false
////                isDeviceVerified = false
////                isVerificationProcessDone = false
////
////                onDeviceConnectionStateChangeListener?.onDeviceDisconnected()
//////                if (::ecgTestInstance.isInitialized)
//////                    ecgTestInstance.onDeviceDisconnected()
////            }
////
////            override fun onReceivedData(data: String) {
////                print("RECEIVED FROM KOTLIN"+data)
////                populateDeviceInfo(data)
////            }
////
////        })
//
//    }
//    private fun getDeviceVariantString(): String {
//        var deviceVariant = ""
//        deviceInfo.let {
//            if (it != null) {
//                deviceVariant = it.deviceVariant.name
//            }
//        }
//        return deviceVariant
//    }
//    private fun populateDeviceInfo(data: String) {
//        deviceInfo = DeviceInfo()
//        if (sendCommand == "c") {
//            val dataArray = data.split(Regex("-"))
//            if (dataArray[2].substring(0, 4).equals("#b2b", true)) {
////                if (dataArray[2].substring(4) == masterKey) {
//                    isDeviceVerified =
//                        (dataArray[0].contains(Regex("spdn")) || dataArray[0].contains(
//                            Regex("splg")
//                        ) )
//                                || (dataArray[0].contains(Regex("spne")) )
//                                || (dataArray[0].contains(Regex("sppr")) )
//                    isVerificationProcessDone = true
//                    deviceInfo.apply {
//                        if (this!=null){
//                            deviceVariant = mapDeviceVariantToVariantEnum(dataArray[0])
//                            firmwareVersion = dataArray[1]
//                            deviceMId = dataArray[3]
//                            deviceId = dataArray[2]
//                        }
//                    }
//
//                    if (isDeviceVerified) onDeviceConnectionStateChangeListener?.onDeviceConnected(
//                        deviceInfo!!
//                    )
////                }
//            } else {
//                isDeviceVerified =
//                    (dataArray[0].contains(Regex("spdn")) || dataArray[0].contains(
//                        Regex("splg")
//                    ) )
//                            || (dataArray[0].contains(Regex("spne")) )
//                            || (dataArray[0].contains(Regex("sppr")) )
//                isVerificationProcessDone = true
//
//                deviceInfo.apply {
//                    if (this!=null){
//                        deviceVariant = mapDeviceVariantToVariantEnum(dataArray[0])
//                        firmwareVersion = dataArray[1]
//
//                        if (dataArray.size > 4) {
//                            val deviceId = data.substring(
//                                dataArray[0].length + 1 + dataArray[1].length + 1,
//                                dataArray[0].length + 1 + dataArray[1].length + 1 + 20
//                            )
//                            this.deviceId = deviceId
//                            deviceMId = data.substring(
//                                dataArray[0].length + 1 + dataArray[1].length + 1 + deviceId.length + 1,
//                                data.length
//                            )
//                        } else {
//                            deviceMId = dataArray[3]
//                            deviceId = dataArray[2]
//                        }
//                    }
//                }
//                print(deviceInfo)
//
//                if (isDeviceVerified) onDeviceConnectionStateChangeListener?.onDeviceConnected(
//                    deviceInfo!!
//                )
//
////                        deviceInfo.deviceVariant = dataArray[0]
////                        deviceInfo.firmwareVersion = dataArray[1]
////                        if (dataArray.size > 4) {
////                            val deviceId = data.substring(
////                                dataArray[0].length + 1 + dataArray[1].length + 1,
////                                dataArray[0].length + 1 + dataArray[1].length + 1 + 20
////                            )
////                            deviceInfo.deviceId = deviceId
////                            deviceInfo.deviceMId = data.substring(
////                                dataArray[0].length + 1 + dataArray[1].length + 1 + deviceId.length + 1,
////                                data.length
////                            )
////                        } else {
////                            deviceInfo.deviceMId = dataArray[3]
////                            deviceInfo.deviceId = dataArray[2]
////                        }
////                        mixPanelHelper.sendToMixpanel(
////                            eventName = DEVICE_VERIFIED,
////                            key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE),
////                            value = arrayListOf(masterKey, deviceInfo.deviceVariant)
////                        )
////                        if (isDeviceVerified) onDeviceConnectionStateChangeListener?.onDeviceVerified(
////                            deviceInfo
////                        )
//            }
//            sendCommand = ""
//        }else{
//            onDeviceConnectionStateChangeListener!!.onDataReceived(data)
//        }}
//    private fun mapDeviceVariantToVariantEnum(deviceVariant:String):SpandanDeviceVariant{
//        if (deviceVariant.contains("sppr"))
//            return SpandanDeviceVariant.SPANDAN_PRO
//        else if (deviceVariant.contains("spne"))
//            return SpandanDeviceVariant.SPANDAN_NEO
//        else
//            return SpandanDeviceVariant.SPANDAN_LEGACY
//    }
//}
//
