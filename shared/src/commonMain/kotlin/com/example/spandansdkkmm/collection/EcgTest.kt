//package `in`.sunfox.healthcare.commons.android.spandan_sdk.collection
//
//import com.example.ecg_processor_kmm.core.EcgProcessor
//import com.example.ecg_processor_kmm.core.models.EcgProcessorResult
//import com.example.ecg_processor_kmm.core.processing.ECGProcessing
//import com.example.ecg_processor_kmm.core.processing.Filters
//import com.example.ecg_processor_kmm.core.processing.TwelveLeadDetection
//import com.example.ecg_processor_kmm.core.processing.heart_risk_calculator.matrices.processor.ProcessorType
//import com.example.spandansdkkmm.Const.CANCEL_TEST
//import com.example.spandansdkkmm.Const.CONNECTED_DEVICE_TYPE
//import com.example.spandansdkkmm.Const.ERROR_DEVICE_DISCONNECTED
//import com.example.spandansdkkmm.Const.ERROR_TEST_NOT_VALID
//import com.example.spandansdkkmm.Const.MASTER_KEY
//import com.example.spandansdkkmm.Const.POSITION
//import com.example.spandansdkkmm.Const.POSITION_RECORDING_COMPLETE
//import com.example.spandansdkkmm.Const.REASON
//import com.example.spandansdkkmm.Const.RECORDING_STARTED
//import com.example.spandansdkkmm.Const.TEST_CANCELED_BY_USER
//import com.example.spandansdkkmm.Const.TEST_FAILED
//import com.example.spandansdkkmm.Const.TEST_STARTED
//import com.example.spandansdkkmm.Const.TEST_START_CALLED
//import com.example.spandansdkkmm.Const.TEST_TYPE
//import com.example.spandansdkkmm.ReportTypes.REPORT_HRV
//import com.example.spandansdkkmm.ReportTypes.REPORT_HYPERKALEMIA
//import com.example.spandansdkkmm.ReportTypes.REPORT_LEAD_TWO
//import com.example.spandansdkkmm.ReportTypes.REPORT_TWELVE_LEAD
//import com.example.spandansdkkmm.collection.EcgTestCallback
//import com.example.spandansdkkmm.conclusion.EcgReport
//import com.example.spandansdkkmm.connection.SpandanSDKException
//import com.example.spandansdkkmm.enums.EcgPosition
//import com.example.spandansdkkmm.enums.EcgTestType
//import com.example.spandansdkkmm.enums.SpandanException
//import com.example.spandansdkkmm.getCommunicator
//import `in`.sunfox.healthcare.commons.android.spandan_sdk.connection.DeviceInfo
//import `in`.sunfox.healthcare.commons.android.spandan_sdk.enums.SpandanException
//import `in`.sunfox.healthcare.java.commons.ecg_processor.conclusions.conclusion.HyperkalemiaConclusion
//import `in`.sunfox.healthcare.java.commons.ecg_processor.conclusions.data.EcgData
//import `in`.sunfox.healthcare.java.commons.ecg_processor.conclusions.data.HrvData
//import `in`.sunfox.healthcare.java.commons.ecg_processor.conclusions.data.LeadTwoData
//import `in`.sunfox.healthcare.java.commons.ecg_processor.conclusions.data.SevenLeadData
//import `in`.sunfox.healthcare.java.commons.ecg_processor.conclusions.data.TwelveLeadData
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.launch
//
//
//class EcgTest(
//    private val masterKey: String,
//    private val ecgTestType: EcgTestType,
//    private val ecgTestCallback: EcgTestCallback,
//    val deviceInfo: DeviceInfo,
//    private val mixPanelHelper: MixPanelHelper,
//    private val verifierToken:String
//) {
//
//    private val randomUUID = UUID.randomUUID().toString()
//    val uniqueId : String = System.currentTimeMillis().toString()+UUID.randomUUID().toString().substring(randomUUID.length-4,randomUUID.length)
//
//    private var _isTestCompleted: MutableStateFlow<Boolean> = MutableStateFlow(false)
//
//    val isTestCompleted : StateFlow<Boolean> = _isTestCompleted
//
//    private val TAG = "EcgTest.TAG"
//
//    companion object {
//
//        private const val SINGLE_LEAD_TEST_DURATION = 10 * 1000L //10000 milliseconds => 10 seconds
//        private const val HRV_LEAD_TEST_DURATION =
//            60 * 5 * 1000L // 300000 milliseconds => 300 seconds => 5 minutes
//    }
//
//    private var isTestInProgress = false
//
//    private val testDuration =
//        if (ecgTestType == EcgTestType.HRV) HRV_LEAD_TEST_DURATION else SINGLE_LEAD_TEST_DURATION
//    private val countDownTimer = object : CountDownTimer(
//        testDuration,
//        1000
//    ) {
//        override fun onTick(millisUntilFinished: Long) {
//            val time = millisUntilFinished / 1000
//            ecgTestCallback.onElapsedTimeChanged(
//                elapsedTime = (testDuration / 1000) - time, remainingTime = testDuration / 1000
//            )
//        }
//
//        override fun onFinish() {
//            isTestInProgress = false
//            val deviceVariant = Utility.mapVariant(deviceInfo.deviceVariant)
//            getCommunicator().sendCommand(
//          if (deviceVariant == "spdn") "0" else if (deviceVariant == "spne" || deviceVariant == "sppr") "STP" else if (deviceVariant == "splg") "0" else "")
//            if (isTestValid()) {
//                mixPanelHelper.sendToMixpanel(
//                    eventName = POSITION_RECORDING_COMPLETE,
//                    key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE, POSITION),
//                    value = arrayListOf(
//                        masterKey,
//                        deviceInfo.deviceVariant.name,
//                        ecgTestType.name,
//                        currentSelectedEcgPosition.name
//                    )
//                )
//                mixPanelHelper.sendTimingEvent(
//                    eventName = RECORDING_STARTED,
//                    key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE
////                        , POSITION
//                    ),
//                    value = arrayListOf(
//                        masterKey,
//                        deviceInfo.deviceVariant.name,
//                        ecgTestType.name,
////                        currentSelectedEcgPosition.name
//                    ),
//                    false
//                )
//                ecgTestCallback.onPositionRecordingCompleted(
//                    ecgPosition = currentSelectedEcgPosition,
//                    ecgPoints = ecgData[currentSelectedEcgPosition]
//                )
//            }
//            else {
//                mixPanelHelper.sendToMixpanel(
//                    eventName = TEST_FAILED,
//                    key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE, REASON),
//                    value = arrayListOf(
//                        masterKey,
//                        deviceInfo.deviceVariant.name,
//                        ecgTestType.name,
//                        "ecg test not valid."
//                    )
//                )
//                ecgTestCallback.onTestFailed(statusCode = ERROR_TEST_NOT_VALID)
//            }
//        }
//    }
//
//    private lateinit var currentSelectedEcgPosition: EcgPosition
//    private var ecgData: HashMap<EcgPosition, ArrayList<Double>> = hashMapOf()
//
//    val _ecgData = ecgData
//
//    private fun setTestCompleteStatus(isTestComplete:Boolean){
//        CoroutineScope(IO).launch {
//            _isTestCompleted.emit(isTestComplete)
//        }
//    }
//
//    fun completeTest(){
//        if (!isTestCompleted.value){
//            if (checkForDataValidation(ecgData)!=null) {
//                setTestCompleteStatus(false)
//            }
//            else {
//                setTestCompleteStatus(true)
//                /**
//                 * call log test complete log api.*/
//                RetrofitHelper()
//                    .getRetrofitInstance()
//                    .pushTestCompleteLog(
//                        authorization = verifierToken,
//                        apiKey = masterKey,
//                        testCompleteLogBody = TestCompleteLogBody(
//                            id = uniqueId,
//                            reportType = ecgTestType.name,
//                            firmwareVersion = deviceInfo.firmwareVersion!!,
//                            sdkVersion = BuildConfig.sdk_version
//                        )
//                    ).enqueue(object : Callback<LogResponse>{
//                        override fun onResponse(
//                            call: Call<LogResponse>,
//                            response: Response<LogResponse>,
//                        ) {
//
//                        }
//
//                        override fun onFailure(call: Call<LogResponse>, t: Throwable) {
//
//                        }
//
//                    })
//                ecgTestCallback.onEcgTestCompleted(hashMap = ecgData)
//            }
//        }
//    }
//
//    fun initializeLead(ecgPosition: EcgPosition) {
//        getCommunicator().sendCommand("SET_LEAD_$ecgPosition")
////        SeriCom.sendCommand()
//    }
//
//    fun start(ecgPosition: EcgPosition) {
//
//        /**
//         * if the last digit of the unique id is 1(TRUE)
//         * i.e. test is completed (all leads)
//         * in this case user not allowed to use same instance of the ecg test for the test either he/she will generate the report or create a new test.
//         * last digit of the unique id will only be modified in the validateTest() which will user call before proceeding to generate report.*/
//        if(isTestCompleted.value)
//            throw SpandanSDKException(SpandanException.IllegalStateException.toString().plus(": Already completed the test,Please create a new test."))
//
//
//        if (isTestInProgress) {
//            val exception =
//                SpandanSDKException("${SpandanException.IllegalStateException}: ${currentSelectedEcgPosition.name} lead already in progress.")
//            mixPanelHelper.sendToMixpanel(
//                eventName = TEST_FAILED,
//                key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE, REASON),
//                value = arrayListOf(
//                    masterKey,
//                    deviceInfo.deviceVariant.name,
//                    ecgTestType.name,
//                    exception.toString()
//                )
//            )
//            throw exception
//        }
//        this.currentSelectedEcgPosition = ecgPosition
//        if ((ecgTestType == EcgTestType.HRV || ecgTestType == EcgTestType.LEAD_TWO) && ecgPosition != EcgPosition.LEAD_2) {
//            val exception =
//                SpandanSDKException("${SpandanException.IllegalLeadException}: for $ecgTestType selected lead must be Lead_II")
//            mixPanelHelper.sendToMixpanel(
//                eventName = TEST_FAILED,
//                key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE, REASON),
//                value = arrayListOf(
//                    masterKey,
//                    deviceInfo.deviceVariant.name,
//                    ecgTestType.name,
//                    exception.toString()
//                )
//            )
//            throw exception
//        } else if (ecgTestType == EcgTestType.HYPERKALEMIA && this.currentSelectedEcgPosition == EcgPosition.LEAD_1) {
//            val exception =
//                SpandanSDKException("${SpandanException.IllegalLeadException}: for $ecgTestType only v1 to v6 and lead II is valid lead")
//            mixPanelHelper.sendToMixpanel(
//                eventName = TEST_FAILED,
//                key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE, REASON),
//                value = arrayListOf(
//                    masterKey,
//                    deviceInfo.deviceVariant.name,
//                    ecgTestType.name,
//                    exception.toString()
//                )
//            )
//            throw exception
//        } else {
//            if (!getCommunicator().getDeviceConnected()) {
//                val exception =
//                    SpandanSDKException("${SpandanException.DeviceNotConnectedException}: please connect the device.")
//                mixPanelHelper.sendToMixpanel(
//                    eventName = TEST_FAILED,
//                    key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE, REASON),
//                    value = arrayListOf(
//                        masterKey,
//                        deviceInfo.deviceVariant.name,
//                        ecgTestType.name,
//                        exception.toString()
//                    )
//                )
//                throw exception
//            } else {
//                mixPanelHelper.sendToMixpanel(
//                    eventName = TEST_START_CALLED,
//                    key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE
////                        , POSITION
//                    ),
//                    value = arrayListOf(
//                        masterKey,
//                        deviceInfo.deviceVariant.name,
//                        ecgTestType.name,
////                        ecgPosition.name
//                    )
//                )
//                mixPanelHelper.sendTimingEvent(
//                    eventName = RECORDING_STARTED,
//                    key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE
////                        , POSITION
//                    ),
//                    value = arrayListOf(
//                        masterKey,
//                        deviceInfo.deviceVariant.name,
//                        ecgTestType.name,
////                        currentSelectedEcgPosition.name
//                    ),
//                    true
//                )
//                getCommunicator().sendCommand(when (Utility.mapVariant(deviceInfo.deviceVariant)) {
//                    "spdn" -> "1"
//                    "spne" -> "STA"
//                    "splg" -> "1"
//                    "sppr" -> "STA_" + ecgPosition.name
//                    else -> ""
//                })
//
//                if (ecgData[ecgPosition] == null)
//                    ecgData[ecgPosition] = arrayListOf()
//                else
//                    ecgData[ecgPosition]!!.clear()
//                if (ecgTestType != EcgTestType.LIVE_MONITOR)
//                    countDownTimer.start()
//                ecgTestCallback.onTestStarted(ecgPosition = currentSelectedEcgPosition)
//                mixPanelHelper.sendToMixpanel(
//                    eventName = TEST_STARTED,
//                    key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE, POSITION),
//                    value = arrayListOf(
//                        masterKey,
//                        deviceInfo.deviceVariant.name,
//                        ecgTestType.name,
//                        ecgPosition.name
//                    )
//                )
//                isTestInProgress = true
//            }
//        }
//
//    }
//
//    private fun setEcgData(ecgPosition: EcgPosition, data: Double) {
//        val list = this.ecgData[ecgPosition]
//
//        if (list == null) {
//            this.ecgData[ecgPosition] = ArrayList()
//        }
//        this.ecgData[ecgPosition]!!.add(data)
//    }
//
//    private fun getEcgData(ecgPosition: EcgPosition): ArrayList<Double> {
//        return ecgData[ecgPosition]!!
//    }
//
//    fun getEcgDataOfSelectedLead() =
//        if (::currentSelectedEcgPosition.isInitialized) ecgData[currentSelectedEcgPosition] else arrayListOf()
//
//    fun isTestValid(): Boolean =
//        ECGProcessing.isEcgSignalCompatibleForProcessing(getEcgData(currentSelectedEcgPosition))
//
//    fun onReceiveData(data: String?) {
//        if (data != null) {
//            setEcgData(currentSelectedEcgPosition, data.toDouble())
//            ecgTestCallback.onReceivedData(data = data)
//        }
//    }
//
//    fun onDeviceDisconnected() {
//        if (isTestInProgress) {
//            isTestInProgress = false
//            countDownTimer.cancel()
//            ecgTestCallback.onTestFailed(statusCode = ERROR_DEVICE_DISCONNECTED)
//        }
//    }
//
//    fun cancel() {
//        if (isTestInProgress) {
//            isTestInProgress = false
//            countDownTimer.cancel()
//            getCommunicator().sendCommand(
//                if (Utility.mapVariant(deviceInfo.deviceVariant) == "spdn") "0"
//                else if (Utility.mapVariant(deviceInfo.deviceVariant) == "spne" || Utility.mapVariant(deviceInfo.deviceVariant) == "sppr") "STP"
//                else "0"
//            )
//
//            mixPanelHelper.sendToMixpanel(
//                eventName = CANCEL_TEST,
//                key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE),
//                value = arrayListOf(masterKey, deviceInfo.deviceVariant.name, ecgTestType.name)
//            )
//            ecgTestCallback.onTestFailed(statusCode = TEST_CANCELED_BY_USER)
//        }
//    }
//
//    private fun checkTwelveLeadTestCompletion(twelveLeadData: TwelveLeadData): Boolean {
//        return twelveLeadData.lead2Data.size != 0
//                &&
//                twelveLeadData.lead1Data.size != 0
//                &&
//                twelveLeadData.v1Data.size != 0
//                &&
//                twelveLeadData.v2Data.size != 0
//                &&
//                twelveLeadData.v3Data.size != 0
//                &&
//                twelveLeadData.v4Data.size != 0
//                &&
//                twelveLeadData.v5Data.size != 0
//                &&
//                twelveLeadData.v6Data.size != 0
//    }
//
//    private fun checkTwelveLeadTestCompletion(): Boolean {
//        return ecgData[EcgPosition.LEAD_2].let {
//            if (it != null) it.size != 0 else false
//        }
//                &&
//                ecgData[EcgPosition.LEAD_1].let {
//                    if (it != null) it.size != 0 else false
//                }
//                &&
//                ecgData[EcgPosition.V1].let {
//                    if (it != null) it.size != 0 else false
//                }
//                &&
//                ecgData[EcgPosition.V2].let {
//                    if (it != null) it.size != 0 else false
//                }
//                &&
//                ecgData[EcgPosition.V3].let {
//                    if (it != null) it.size != 0 else false
//                }
//                &&
//                ecgData[EcgPosition.V4].let {
//                    if (it != null) it.size != 0 else false
//                }
//                &&
//                ecgData[EcgPosition.V5].let {
//                    if (it != null) it.size != 0 else false
//                }
//                &&
//                ecgData[EcgPosition.V6].let {
//                    if (it != null) it.size != 0 else false
//                }
//    }
//
//    private fun checkSevenLeadTestCompletion(sevenLeadData: SevenLeadData): Boolean {
//        return sevenLeadData.lead2Data.size != 0
//                &&
//                sevenLeadData.v1Data.size != 0
//                &&
//                sevenLeadData.v2Data.size != 0
//                &&
//                sevenLeadData.v3Data.size != 0
//                &&
//                sevenLeadData.v4Data.size != 0
//                &&
//                sevenLeadData.v5Data.size != 0
//                &&
//                sevenLeadData.v6Data.size != 0
//    }
//
//    private fun checkSevenLeadTestCompletion(): Boolean {
//        return ecgData[EcgPosition.LEAD_2].let {
//            if (it != null) it.size != 0 else false
//        }
//                &&
//                ecgData[EcgPosition.V1].let {
//                    if (it != null) it.size != 0 else false
//                }
//                &&
//                ecgData[EcgPosition.V2].let {
//                    if (it != null) it.size != 0 else false
//                }
//                &&
//                ecgData[EcgPosition.V3].let {
//                    if (it != null) it.size != 0 else false
//                }
//                &&
//                ecgData[EcgPosition.V4].let {
//                    if (it != null) it.size != 0 else false
//                }
//                &&
//                ecgData[EcgPosition.V5].let {
//                    if (it != null) it.size != 0 else false
//                }
//                &&
//                ecgData[EcgPosition.V6].let {
//                    if (it != null) it.size != 0 else false
//                }
//    }
//
//    private fun checkForLeadII(leadData: LeadTwoData): Boolean {
//        return leadData.lead2Data.size != 0
//    }
//
//    private fun checkForHrv(leadData: HrvData): Boolean {
//        return leadData.fiveMinuteData.size != 0
//    }
//
//    private fun checkForLeadII(): Boolean {
//        return ecgData[EcgPosition.LEAD_2].let {
//            if (it != null) it.size != 0 else false
//        }
//    }
//
//    private fun getEcgDataFromHashMap(ecgPoints: HashMap<EcgPosition, ArrayList<Double>>): EcgData {
//        return when (ecgTestType) {
//            EcgTestType.LEAD_TWO -> {
//                if (ecgPoints[EcgPosition.LEAD_2].isNullOrEmpty())
//                    throw SpandanSDKException("${SpandanException.InsufficientDataException} must provide lead II data.")
//                LeadTwoData(ecgPoints[EcgPosition.LEAD_2] as ArrayList<Double> /* = java.util.ArrayList<kotlin.Double> */)
//            }
//
//            EcgTestType.TWELVE_LEAD -> {
//                if (ecgPoints[EcgPosition.V1].isNullOrEmpty() || ecgPoints[EcgPosition.V2].isNullOrEmpty() || ecgPoints[EcgPosition.V3].isNullOrEmpty() || ecgPoints[EcgPosition.V4].isNullOrEmpty() || ecgPoints[EcgPosition.V5].isNullOrEmpty() || ecgPoints[EcgPosition.V6].isNullOrEmpty() || ecgPoints[EcgPosition.LEAD_1].isNullOrEmpty() || ecgPoints[EcgPosition.LEAD_2].isNullOrEmpty())
//                    throw SpandanSDKException("${SpandanException.InsufficientDataException} must provide all lead data.")
//                TwelveLeadData(
//                    v1Data = ecgPoints[EcgPosition.V1]!!,
//                    v2Data = ecgPoints[EcgPosition.V2]!!,
//                    v3Data = ecgPoints[EcgPosition.V3]!!,
//                    v4Data = ecgPoints[EcgPosition.V4]!!,
//                    v5Data = ecgPoints[EcgPosition.V5]!!,
//                    v6Data = ecgPoints[EcgPosition.V6]!!,
//                    lead1Data = ecgPoints[EcgPosition.LEAD_1]!!,
//                    lead2Data = ecgPoints[EcgPosition.LEAD_2]!!,
//                    avfData = arrayListOf(),
//                    avrData = arrayListOf(),
//                    lead3Data = arrayListOf(),
//                    avlData = arrayListOf()
//                )
//            }
//
//            EcgTestType.HYPERKALEMIA -> {
//                if (ecgPoints[EcgPosition.V1].isNullOrEmpty() || ecgPoints[EcgPosition.V2].isNullOrEmpty() || ecgPoints[EcgPosition.V3].isNullOrEmpty() || ecgPoints[EcgPosition.V4].isNullOrEmpty() || ecgPoints[EcgPosition.V5].isNullOrEmpty() || ecgPoints[EcgPosition.V6].isNullOrEmpty() || ecgPoints[EcgPosition.LEAD_2].isNullOrEmpty())
//                    throw SpandanSDKException("${SpandanException.InsufficientDataException} must provide all lead data.")
//                SevenLeadData(
//                    v1Data = ecgPoints[EcgPosition.V1]!!,
//                    v2Data = ecgPoints[EcgPosition.V2]!!,
//                    v3Data = ecgPoints[EcgPosition.V3]!!,
//                    v4Data = ecgPoints[EcgPosition.V4]!!,
//                    v5Data = ecgPoints[EcgPosition.V5]!!,
//                    v6Data = ecgPoints[EcgPosition.V6]!!,
//                    lead2Data = ecgPoints[EcgPosition.LEAD_2]!!
//                )
//            }
//
//            EcgTestType.HRV -> {
//                if (ecgPoints[EcgPosition.LEAD_2].isNullOrEmpty())
//                    throw SpandanSDKException("${SpandanException.InsufficientDataException} must provide lead II data.")
//                HrvData(
//                    fiveMinuteData = ecgPoints[EcgPosition.LEAD_2]!!,
//                    fftData = arrayListOf()
//                )
//            }
//
//            else -> {
//                HrvData(
//                    fiveMinuteData = ecgPoints[EcgPosition.LEAD_2]!!,
//                    fftData = arrayListOf()
//                )
//            }
//        }
//    }
//
//    fun checkForDataValidation(ecgData: HashMap<EcgPosition, ArrayList<Double>>): SpandanSDKException? {
//        when (ecgTestType) {
//            EcgTestType.LEAD_TWO -> {
//                if (!checkForLeadII(getEcgDataFromHashMap(ecgData) as LeadTwoData))
//                    return SpandanSDKException("${SpandanException.InsufficientDataException}: Lead II position data is incomplete or invalid.")
//            }
//
//            EcgTestType.TWELVE_LEAD -> {
//                if (!checkTwelveLeadTestCompletion(getEcgDataFromHashMap(ecgData) as TwelveLeadData))
//                    return SpandanSDKException("${SpandanException.InsufficientDataException}:Twelve Lead data is incomplete or invalid.")
//            }
//
//            EcgTestType.HRV -> {
//                if (!checkForHrv(getEcgDataFromHashMap(ecgData) as HrvData))
//                    return SpandanSDKException("${SpandanException.InsufficientDataException}: Hrv position data is incomplete or invalid.")
//            }
//
//            EcgTestType.HYPERKALEMIA -> {
//                if (!checkSevenLeadTestCompletion(getEcgDataFromHashMap(ecgData) as SevenLeadData))
//                    return SpandanSDKException("${SpandanException.InsufficientDataException}: Seven Lead data for Hyperkalemia is incomplete or invalid.")
//            }
//
//            EcgTestType.LIVE_MONITOR -> {}
//        }
//        return null
//    }
//
//    fun proceedReport(
//        userAge: Int,
//        ecgData: HashMap<EcgPosition, ArrayList<Double>>,
//    ): EcgReport {
//        return generateReport(userAge, ecgData)
//    }
//
//    private fun generateReport(
//        userAge: Int,
//        ecgData: HashMap<EcgPosition, ArrayList<Double>>,
//    ): EcgReport {
//        val ecgReport: EcgReport =
//            when (ecgTestType) {
//                EcgTestType.LEAD_TWO -> openLeadIIReport(getEcgDataFromHashMap(ecgData) as LeadTwoData)
//                EcgTestType.HRV -> openHrvReport(userAge, getEcgDataFromHashMap(ecgData) as HrvData)
//                EcgTestType.TWELVE_LEAD -> openTwelveLeadReport(
//                    userAge,
//                    getEcgDataFromHashMap(ecgData) as TwelveLeadData
//                )
//
//                else -> openSevenLeadReport(getEcgDataFromHashMap(ecgData) as SevenLeadData)
//            }
//        return ecgReport
//    }
//
//    private fun openLeadIIReport(leadIIData: LeadTwoData): EcgReport {
//        var processorResult: EcgProcessorResult? = null
//        processorResult =
//            EcgProcessor(
//                ProcessorType.LEAD_TWO, leadIIData, applyFilter = true
//            ).process()
//        val characteristics = processorResult.characteristics[0]!!
//        val t = processorResult.conclusion
//        return EcgReport(
//            reportType = REPORT_LEAD_TWO,
//            timeStamp = System.currentTimeMillis().toString(),
//            ecgCharacteristics = characteristics,
//            conclusion = t,
//            ecgData = leadIIData
//        )
//    }
//
//    private fun openHrvReport(userAge: Int, ecgData: EcgData): EcgReport {
//        val finalHrvData = ecgData as HrvData
//        var processorResult: EcgProcessorResult? = null
//        processorResult =
//            EcgProcessor(
//                ProcessorType.HRV, finalHrvData
//            ).process(age = userAge)
//
//        return EcgReport(
//            reportType = REPORT_HRV,
//            timeStamp = System.currentTimeMillis().toString(),
//            ecgData = finalHrvData,
//            ecgCharacteristics = processorResult.characteristics[0]!!,
//            conclusion = processorResult.conclusion,
//        )
//    }
//
//    private fun openSevenLeadReport(ecgData: EcgData): EcgReport {
//        val sevenLeadData = ecgData as SevenLeadData
//        val processorResult: EcgProcessorResult?
//        processorResult =
//            EcgProcessor(
//                ProcessorType.SEVEN_LEAD, sevenLeadData
//            ).process()
//        val characteristics = processorResult.characteristics[0]!!
//        val conclusion = processorResult.conclusion as HyperkalemiaConclusion
//        return EcgReport(
//            reportType = REPORT_HYPERKALEMIA,
//            timeStamp = System.currentTimeMillis().toString(),
//            ecgCharacteristics = characteristics,
//            conclusion = conclusion,
//            ecgData = ecgData
//        )
//    }
//
//    private fun openTwelveLeadReport(userAge: Int, ecgData: EcgData): EcgReport {
//        val tempData = ecgData as TwelveLeadData
//        val twelveLeadData = tempData.copy()
//        twelveLeadData.lead1Data = Filters.movingAverage(twelveLeadData.lead1Data)
//        twelveLeadData.lead2Data = Filters.movingAverage(twelveLeadData.lead2Data)
//        val augmentedLeadGenerator = EcgProcessor.getAugmentedLeadGenerator(
//            twelveLeadData.lead1Data,
//            twelveLeadData.lead2Data
//        )
//        twelveLeadData.lead1Data = augmentedLeadGenerator.finalLead1Points
//        twelveLeadData.lead2Data = augmentedLeadGenerator.finalLead2Points
//        twelveLeadData.lead3Data = augmentedLeadGenerator.lead3Points
//        twelveLeadData.avfData = augmentedLeadGenerator.aVfPoints
//        twelveLeadData.avlData = augmentedLeadGenerator.aVlPoints
//        twelveLeadData.avrData = augmentedLeadGenerator.aVrPoints
//
//        val twelveLeadDataForProcess = twelveLeadData.copy(
//            lead1Data = Filters.movingAverage((ecgData).lead1Data),
//            lead2Data = Filters.movingAverage((ecgData).lead2Data)
//        )
//
//        var processorResult: EcgProcessorResult? = null
//        processorResult =
//            EcgProcessor(
//                ProcessorType.TWELVE_LEAD,
//                twelveLeadDataForProcess,
//                applyFilter = false,
//                adjustRPeaks = false
//            ).process(
//                augmentedLeadGenerator = augmentedLeadGenerator,
//                age = userAge
//            )
//
//        val conclusion = processorResult.conclusion
//        val characteristics =
//            processorResult.characteristics[TwelveLeadDetection.EcgPosition.LEAD_2]!!
//        return EcgReport(
//            reportType = REPORT_TWELVE_LEAD,
//            timeStamp = System.currentTimeMillis().toString(),
//            ecgCharacteristics = characteristics,
//            conclusion = conclusion,
//            ecgData = twelveLeadData
//        )
//    }
//
//    fun getEcgTestType() = ecgTestType
//}