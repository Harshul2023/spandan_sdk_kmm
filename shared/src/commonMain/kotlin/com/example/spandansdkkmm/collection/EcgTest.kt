
import EcgTestLogData
import com.benasher44.uuid.UUID

import com.example.ecg_processor_kmm.core.EcgProcessor
import com.example.ecg_processor_kmm.core.models.EcgProcessorResult
import com.example.ecg_processor_kmm.core.processing.ECGProcessing
import com.example.ecg_processor_kmm.core.processing.Filters
import com.example.ecg_processor_kmm.core.processing.TwelveLeadDetection
import com.example.ecg_processor_kmm.core.processing.heart_risk_calculator.matrices.processor.ProcessorType
import com.example.spandansdkkmm.Const.CANCEL_TEST
import com.example.spandansdkkmm.Const.CONNECTED_DEVICE_TYPE
import com.example.spandansdkkmm.Const.ERROR_DEVICE_DISCONNECTED
import com.example.spandansdkkmm.Const.MASTER_KEY
import com.example.spandansdkkmm.Const.POSITION
import com.example.spandansdkkmm.Const.POSITION_RECORDING_COMPLETE
import com.example.spandansdkkmm.Const.RECORDING_STARTED
import com.example.spandansdkkmm.Const.TEST_TYPE
import com.example.spandansdkkmm.Const.TEST_FAILED
import com.example.spandansdkkmm.Const.REASON
import com.example.spandansdkkmm.Const.ERROR_TEST_NOT_VALID
import com.example.spandansdkkmm.Const.TEST_CANCELED_BY_USER
import com.example.spandansdkkmm.Const.TEST_STARTED
import com.example.spandansdkkmm.Const.TEST_START_CALLED
import com.example.spandansdkkmm.ReportTypes.REPORT_HRV
import com.example.spandansdkkmm.ReportTypes.REPORT_HYPERKALEMIA
import com.example.spandansdkkmm.ReportTypes.REPORT_LEAD_TWO
import com.example.spandansdkkmm.ReportTypes.REPORT_TWELVE_LEAD
import com.example.spandansdkkmm.collection.CountDownTimer
import com.example.spandansdkkmm.collection.EcgTestCallback
import com.example.spandansdkkmm.conclusion.EcgReport
import com.example.spandansdkkmm.connection.SpandanSDKException
import com.example.spandansdkkmm.enums.EcgPosition
import com.example.spandansdkkmm.enums.EcgTestType
import com.example.spandansdkkmm.getCommunicator
import com.example.spandansdkkmm.retrofit_helper.EcgTestLogResponse

import `in`.sunfox.healthcare.java.commons.ecg_processor.conclusions.conclusion.HyperkalemiaConclusion
import `in`.sunfox.healthcare.java.commons.ecg_processor.conclusions.data.EcgData
import `in`.sunfox.healthcare.java.commons.ecg_processor.conclusions.data.HrvData
import `in`.sunfox.healthcare.java.commons.ecg_processor.conclusions.data.LeadTwoData
import `in`.sunfox.healthcare.java.commons.ecg_processor.conclusions.data.SevenLeadData
import `in`.sunfox.healthcare.java.commons.ecg_processor.conclusions.data.TwelveLeadData
import com.example.spandansdkkmm.util.Utility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

import com.benasher44.uuid.uuid4


class EcgTest(
    private val masterKey: String,
    private val ecgTestType: EcgTestType,
    private val ecgTestCallback: EcgTestCallback,
    val deviceInfo: DeviceInfo,
    private val mixPanelHelper: MixPanelHelper?,
    private val verifierToken:String?
) {

    private val randomUUID = uuid4().toString()
    val uniqueId : String = getCurrentTimestamp()+randomUUID.substring(randomUUID.length-4,randomUUID.length)

    private var _isTestCompleted: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val isTestCompleted : StateFlow<Boolean> = _isTestCompleted

    private val TAG = "EcgTest.TAG"

    companion object {

        private const val SINGLE_LEAD_TEST_DURATION = 10 * 1000L //10000 milliseconds => 10 seconds
        private const val HRV_LEAD_TEST_DURATION =
            60 * 5 * 1000L // 300000 milliseconds => 300 seconds => 5 minutes
    }

    private var isTestInProgress = false

    private val testDuration =
        if (ecgTestType == EcgTestType.HRV) HRV_LEAD_TEST_DURATION else SINGLE_LEAD_TEST_DURATION
    val countDownTimer = CountDownTimer(
        duration = testDuration,
        interval = 1000L,
        onTick = { millisUntilFinished ->
            val time = millisUntilFinished / 1000
            ecgTestCallback.onElapsedTimeChanged(
                elapsedTime = (testDuration / 1000) - time, remainingTime = millisUntilFinished / 1000
            )
        },
        onFinish = {
            isTestInProgress = false
            val deviceVariant = Utility.mapVariant(deviceInfo.deviceVariant)
            getCommunicator().sendCommand(if (deviceVariant == "spdn") "0" else if (deviceVariant == "spne" || deviceVariant == "sppr") "STP" else if (deviceVariant == "splg") "0" else "")
            if (isTestValid()) {
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
                ecgTestCallback.onPositionRecordingCompleted(
                    ecgPosition = currentSelectedEcgPosition,
                    ecgPoints = ecgData[currentSelectedEcgPosition]
                )
            }
            else {
//                mixPanelHelper.sendToMixpanel(
//                    eventName = TEST_FAILED,
//                    key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE, REASON),
//                    value = arrayListOf(
//                        masterKey,
//                        deviceInfo.deviceVariant.name,
//                        ecgTestType.name,
//                        "ECG data is not valid. Please retake the test."
//                    )
//                )
                ecgData[currentSelectedEcgPosition]?.clear()
                ecgTestCallback.onTestFailed(statusCode = ERROR_TEST_NOT_VALID)
            }
        }
    )

    private lateinit var currentSelectedEcgPosition: EcgPosition
    private var ecgData: HashMap<EcgPosition, ArrayList<Double>> = hashMapOf()

    val _ecgData = ecgData

    private fun setTestCompleteStatus(isTestComplete:Boolean){
        CoroutineScope(Dispatchers.IO).launch {
            _isTestCompleted.emit(isTestComplete)
        }
    }

    suspend fun completeTest() {
        if (!isTestCompleted.value) {
            if (checkForDataValidation(ecgData) != null) {
                setTestCompleteStatus(false)
            } else {
                setTestCompleteStatus(true)
                val helper = RetrofitHelper()
                try {
                    val result = helper.getRetrofitInstance().pushEcgTestLog(
                        authorization = verifierToken,
                        apiKey = masterKey,
                        ecgTestLogData = EcgTestLogData(
                            id = uniqueId,
                            reportType = ecgTestType.name,
                            firmwareVersion = deviceInfo?.firmwareVersion ?: "",
//                            sdkVersion = BuildConfig.sdk_version
                            sdkVersion = "2.0.0"
                        )
                    )

                    ecgTestCallback.onEcgTestCompleted(hashMap = ecgData)


                } catch (e: Exception) {
                    print("Failed to push ECG test log: ${e.message}")
                    // Handle network exceptions or other errors
//                    logger.info("Failed to push ECG test log: ${e.message}")
                }
            }
        }
    }




    fun initializeLead(ecgPosition: EcgPosition) {
        getCommunicator().sendCommand("SET_LEAD_$ecgPosition")
    }

    fun start(ecgPosition: EcgPosition) {

        /**
         * if the last digit of the unique id is 1(TRUE)
         * i.e. test is completed (all leads)
         * in this case user not allowed to use same instance of the ecg test for the test either he/she will generate the report or create a new test.
         * last digit of the unique id will only be modified in the validateTest() which will user call before proceeding to generate report.*/
        if(isTestCompleted.value)
            throw SpandanSDKException(SpandanException.IllegalStateException.name.plus(": The test has already been marked as complete. Create a new test to take a new one."))


        if (isTestInProgress) {
            val exception =
                SpandanSDKException("${SpandanException.IllegalStateException.name}: Cannot start the test. ${currentSelectedEcgPosition.name} lead already in progress. Please cancel or wait for completion")
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
            throw exception
        }
        this.currentSelectedEcgPosition = ecgPosition
        if ((ecgTestType == EcgTestType.HRV || ecgTestType == EcgTestType.LEAD_TWO) && ecgPosition != EcgPosition.LEAD_2) {
            val exception =
                SpandanSDKException("${SpandanException.InvalidLeadSelectedException.name}: For $ecgTestType, selected lead must be Lead II")
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
            throw exception
        } else if (ecgTestType == EcgTestType.HYPERKALEMIA && this.currentSelectedEcgPosition == EcgPosition.LEAD_1) {
            val exception =
                SpandanSDKException("${SpandanException.InvalidLeadSelectedException.name}: For $ecgTestType, selected leads must be any of v1 to v6 and lead II")
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
            throw exception
        } else {
            if (!getCommunicator().getDeviceConnected()) {
                val exception =
                    SpandanSDKException("${SpandanException.DeviceNotConnectedException.name}: Please connect the Spandan device before starting the test.")
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
                throw exception
            } else {
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
                getCommunicator().sendCommand(

                    when (Utility.mapVariant(deviceInfo.deviceVariant)) {
                        "spdn" -> "1"
                        "spne" -> "STA"
                        "splg" -> "1"
                        "sppr" -> "STA_" + ecgPosition.name
                        else -> ""
                    }
                )
                if (ecgData[ecgPosition] == null)
                    ecgData[ecgPosition] = arrayListOf()
                else
                    ecgData[ecgPosition]!!.clear()
                if (ecgTestType != EcgTestType.LIVE_MONITOR)
                    countDownTimer.start()
                ecgTestCallback.onTestStarted(ecgPosition = currentSelectedEcgPosition)
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
                isTestInProgress = true
            }
        }

    }

    private fun setEcgData(ecgPosition: EcgPosition, data: Double) {
        val list = this.ecgData[ecgPosition]

        if (list == null) {
            this.ecgData[ecgPosition] = ArrayList()
        }
        this.ecgData[ecgPosition]!!.add(data)
    }

    private fun getEcgData(ecgPosition: EcgPosition): ArrayList<Double> {
        return ecgData[ecgPosition]!!
    }

//    fun getEcgDataOfSelectedLead() =
//        if (::currentSelectedEcgPosition.isInitialized) ecgData[currentSelectedEcgPosition] else arrayListOf()

    fun isTestValid(): Boolean =
        ECGProcessing.isEcgSignalCompatibleForProcessing(getEcgData(currentSelectedEcgPosition))

    fun onReceiveData(data: String?) {
        if (data != null) {
            setEcgData(currentSelectedEcgPosition, data.toDouble())
            ecgTestCallback.onReceivedData(data = data)
        }
    }

    fun onDeviceDisconnected() {
        if (isTestInProgress) {
            isTestInProgress = false
            countDownTimer.cancel()
            ecgTestCallback.onTestFailed(statusCode = ERROR_DEVICE_DISCONNECTED)
        }
    }

    fun cancel() {
        if (isTestInProgress) {
            isTestInProgress = false
            countDownTimer.cancel()
            getCommunicator().sendCommand(
                if (Utility.mapVariant(deviceInfo.deviceVariant) == "spdn") "0"
                else if (Utility.mapVariant(deviceInfo.deviceVariant) == "spne" || Utility.mapVariant(deviceInfo.deviceVariant) == "sppr") "STP"
                else "0"
            )
//            mixPanelHelper.sendToMixpanel(
//                eventName = CANCEL_TEST,
//                key = arrayListOf(MASTER_KEY, CONNECTED_DEVICE_TYPE, TEST_TYPE),
//                value = arrayListOf(masterKey, deviceInfo.deviceVariant.name, ecgTestType.name)
//            )
            ecgTestCallback.onTestFailed(statusCode = TEST_CANCELED_BY_USER)
        }
    }

    private fun checkTwelveLeadTestCompletion(twelveLeadData: TwelveLeadData): Boolean {
        return twelveLeadData.lead2Data.size != 0
                &&
                twelveLeadData.lead1Data.size != 0
                &&
                twelveLeadData.v1Data.size != 0
                &&
                twelveLeadData.v2Data.size != 0
                &&
                twelveLeadData.v3Data.size != 0
                &&
                twelveLeadData.v4Data.size != 0
                &&
                twelveLeadData.v5Data.size != 0
                &&
                twelveLeadData.v6Data.size != 0
    }

    private fun checkSevenLeadTestCompletion(sevenLeadData: SevenLeadData): Boolean {
        return sevenLeadData.lead2Data.size != 0
                &&
                sevenLeadData.v1Data.size != 0
                &&
                sevenLeadData.v2Data.size != 0
                &&
                sevenLeadData.v3Data.size != 0
                &&
                sevenLeadData.v4Data.size != 0
                &&
                sevenLeadData.v5Data.size != 0
                &&
                sevenLeadData.v6Data.size != 0
    }

    private fun checkForLeadII(leadData: LeadTwoData): Boolean {
        return leadData.lead2Data.size != 0
    }

    private fun checkForHrv(leadData: HrvData): Boolean {
        return leadData.fiveMinuteData.size != 0
    }

    private fun getEcgDataFromHashMap(ecgPoints: HashMap<EcgPosition, ArrayList<Double>>): EcgData {
        return when (ecgTestType) {
            EcgTestType.LEAD_TWO -> {
                if (ecgPoints[EcgPosition.LEAD_2].isNullOrEmpty())
                    throw SpandanSDKException("${SpandanException.InsufficientDataException.name}: Lead II data is null or empty. Required: Not null or empty.")
                LeadTwoData(ecgPoints[EcgPosition.LEAD_2] as ArrayList<Double> /* = java.util.ArrayList<kotlin.Double> */)
            }

            EcgTestType.TWELVE_LEAD -> {
                if (ecgPoints[EcgPosition.V1].isNullOrEmpty() || ecgPoints[EcgPosition.V2].isNullOrEmpty() || ecgPoints[EcgPosition.V3].isNullOrEmpty() || ecgPoints[EcgPosition.V4].isNullOrEmpty() || ecgPoints[EcgPosition.V5].isNullOrEmpty() || ecgPoints[EcgPosition.V6].isNullOrEmpty() || ecgPoints[EcgPosition.LEAD_1].isNullOrEmpty() || ecgPoints[EcgPosition.LEAD_2].isNullOrEmpty())
                    throw SpandanSDKException("${SpandanException.InsufficientDataException.name}: Either of V1 to V6 or Lead I or Lead II is empty. All leads from V1 to V6 and Lead I and Lead II are required for 12L test.")
                TwelveLeadData(
                    v1Data = ecgPoints[EcgPosition.V1] as ArrayList<Double>,
                    v2Data = ecgPoints[EcgPosition.V2] as ArrayList<Double>,
                    v3Data = ecgPoints[EcgPosition.V3] as ArrayList<Double>,
                    v4Data = ecgPoints[EcgPosition.V4] as ArrayList<Double>,
                    v5Data = ecgPoints[EcgPosition.V5] as ArrayList<Double>,
                    v6Data = ecgPoints[EcgPosition.V6] as ArrayList<Double>,
                    lead1Data = ecgPoints[EcgPosition.LEAD_1] as ArrayList<Double>,
                    lead2Data = ecgPoints[EcgPosition.LEAD_2] as ArrayList<Double>,
                    avfData = arrayListOf(),
                    avrData = arrayListOf(),
                    lead3Data = arrayListOf(),
                    avlData = arrayListOf()
                )
            }

            EcgTestType.HYPERKALEMIA -> {
                if (ecgPoints[EcgPosition.V1].isNullOrEmpty() || ecgPoints[EcgPosition.V2].isNullOrEmpty() || ecgPoints[EcgPosition.V3].isNullOrEmpty() || ecgPoints[EcgPosition.V4].isNullOrEmpty() || ecgPoints[EcgPosition.V5].isNullOrEmpty() || ecgPoints[EcgPosition.V6].isNullOrEmpty() || ecgPoints[EcgPosition.LEAD_2].isNullOrEmpty())
                    throw SpandanSDKException("${SpandanException.InsufficientDataException.name}: Either of V1 to V6 or Lead II is empty. All leads from V1 to V6 and Lead II are required for Hyperkalemia test.")
                SevenLeadData(
                    v1Data = ecgPoints[EcgPosition.V1] as ArrayList<Double>,
                    v2Data = ecgPoints[EcgPosition.V2] as ArrayList<Double>,
                    v3Data = ecgPoints[EcgPosition.V3] as ArrayList<Double>,
                    v4Data = ecgPoints[EcgPosition.V4] as ArrayList<Double>,
                    v5Data = ecgPoints[EcgPosition.V5] as ArrayList<Double>,
                    v6Data = ecgPoints[EcgPosition.V6] as ArrayList<Double>,
                    lead2Data = ecgPoints[EcgPosition.LEAD_2] as ArrayList<Double>
                )
            }

            EcgTestType.HRV -> {
                if (ecgPoints[EcgPosition.LEAD_2].isNullOrEmpty())
                    throw SpandanSDKException("${SpandanException.InsufficientDataException.name}: Lead II data is null or empty. Required: Not null or empty.")
                HrvData(
                    fiveMinuteData = ecgPoints[EcgPosition.LEAD_2] as ArrayList<Double>,
                    fftData = arrayListOf()
                )
            }

            else -> {
                HrvData(
                    fiveMinuteData = ecgPoints[EcgPosition.LEAD_2] as ArrayList<Double>,
                    fftData = arrayListOf()
                )
            }
        }
    }

    fun checkForDataValidation(ecgData: HashMap<EcgPosition, ArrayList<Double>>): SpandanSDKException? {
        when (ecgTestType) {
            EcgTestType.LEAD_TWO -> {
                if (!checkForLeadII(getEcgDataFromHashMap(ecgData) as LeadTwoData))
                    return SpandanSDKException("${SpandanException.InsufficientDataException.name}: Lead II data is either incomplete or invalid. Please check the recorded points.")
            }

            EcgTestType.TWELVE_LEAD -> {
                if (!checkTwelveLeadTestCompletion(getEcgDataFromHashMap(ecgData) as TwelveLeadData))
                    return SpandanSDKException("${SpandanException.InsufficientDataException.name}: Twelve Lead data is either incomplete or invalid. Please check the recorded points for all the positions.")
            }

            EcgTestType.HRV -> {
                if (!checkForHrv(getEcgDataFromHashMap(ecgData) as HrvData))
                    return SpandanSDKException("${SpandanException.InsufficientDataException.name}: HRV data is either incomplete or invalid. Please check the recorded points.")
            }

            EcgTestType.HYPERKALEMIA -> {
                if (!checkSevenLeadTestCompletion(getEcgDataFromHashMap(ecgData) as SevenLeadData))
                    return SpandanSDKException("${SpandanException.InsufficientDataException.name}: Seven Lead data for Hyperkalemia is either incomplete or invalid. Please check the recorded points for all the positions.")
            }

            EcgTestType.LIVE_MONITOR -> {}
        }
        return null
    }

    fun proceedReport(
        userAge: Int,
        ecgData: HashMap<EcgPosition, ArrayList<Double>>,
    ): EcgReport {
        return generateReport(userAge, ecgData)
    }

    private fun generateReport(
        userAge: Int,
        ecgData: HashMap<EcgPosition, ArrayList<Double>>,
    ): EcgReport {
        val ecgReport: EcgReport =
            when (ecgTestType) {
                EcgTestType.LEAD_TWO -> openLeadIIReport(getEcgDataFromHashMap(ecgData) as LeadTwoData)
                EcgTestType.HRV -> openHrvReport(userAge, getEcgDataFromHashMap(ecgData) as HrvData)
                EcgTestType.TWELVE_LEAD -> openTwelveLeadReport(
                    userAge,
                    getEcgDataFromHashMap(ecgData) as TwelveLeadData
                )

                else -> openSevenLeadReport(getEcgDataFromHashMap(ecgData) as SevenLeadData)
            }
        return ecgReport
    }

    private fun openLeadIIReport(leadIIData: LeadTwoData): EcgReport {
        var processorResult: EcgProcessorResult? = null
        processorResult =
            EcgProcessor(
                ProcessorType.LEAD_TWO, leadIIData, applyFilter = true
            ).process()
        val characteristics = processorResult.characteristics[0]!!
        val t = processorResult.conclusion
        return EcgReport(
            reportType = REPORT_LEAD_TWO,
            timeStamp = getCurrentTimestamp(),
            ecgCharacteristics = characteristics,
            conclusion = t,
            ecgData = leadIIData
        )
    }

    private fun openHrvReport(userAge: Int, ecgData: EcgData): EcgReport {
        val finalHrvData = ecgData as HrvData
        var processorResult: EcgProcessorResult? = null
        processorResult =
            EcgProcessor(
                ProcessorType.HRV, finalHrvData
            ).process(age = userAge)

        return EcgReport(
            reportType = REPORT_HRV,
            timeStamp = getCurrentTimestamp(),
            ecgData = finalHrvData,
            ecgCharacteristics = processorResult.characteristics[0]!!,
            conclusion = processorResult.conclusion,
        )
    }

    private fun openSevenLeadReport(ecgData: EcgData): EcgReport {
        val sevenLeadData = ecgData as SevenLeadData
        val processorResult: EcgProcessorResult?
        processorResult =
            EcgProcessor(
                ProcessorType.SEVEN_LEAD, sevenLeadData
            ).process()
        val characteristics = processorResult.characteristics[0]!!
        val conclusion = processorResult.conclusion as HyperkalemiaConclusion
        return EcgReport(
            reportType = REPORT_HYPERKALEMIA,
            timeStamp = getCurrentTimestamp(),
            ecgCharacteristics = characteristics,
            conclusion = conclusion,
            ecgData = ecgData
        )
    }

    private fun openTwelveLeadReport(userAge: Int, ecgData: EcgData): EcgReport {
        val tempData = ecgData as TwelveLeadData
        val twelveLeadData = tempData.copy()
        twelveLeadData.lead1Data = Filters.movingAverage(twelveLeadData.lead1Data)
        twelveLeadData.lead2Data = Filters.movingAverage(twelveLeadData.lead2Data)
        val augmentedLeadGenerator = EcgProcessor.getAugmentedLeadGenerator(
            twelveLeadData.lead1Data,
            twelveLeadData.lead2Data
        )
        twelveLeadData.lead1Data = augmentedLeadGenerator.finalLead1Points
        twelveLeadData.lead2Data = augmentedLeadGenerator.finalLead2Points
        twelveLeadData.lead3Data = augmentedLeadGenerator.lead3Points
        twelveLeadData.avfData = augmentedLeadGenerator.aVfPoints
        twelveLeadData.avlData = augmentedLeadGenerator.aVlPoints
        twelveLeadData.avrData = augmentedLeadGenerator.aVrPoints

        val twelveLeadDataForProcess = twelveLeadData.copy(
            lead1Data = Filters.movingAverage((ecgData).lead1Data),
            lead2Data = Filters.movingAverage((ecgData).lead2Data)
        )

        var processorResult: EcgProcessorResult? = null
        processorResult =
            EcgProcessor(
                ProcessorType.TWELVE_LEAD,
                twelveLeadDataForProcess,
                applyFilter = false,
                adjustRPeaks = false
            ).process(
                augmentedLeadGenerator = augmentedLeadGenerator,
                age = userAge
            )

        val conclusion = processorResult.conclusion
        val characteristics =
            processorResult.characteristics[TwelveLeadDetection.EcgPosition.LEAD_2]!!
        return EcgReport(
            reportType = REPORT_TWELVE_LEAD,
            timeStamp = getCurrentTimestamp(),
            ecgCharacteristics = characteristics,
            conclusion = conclusion,
            ecgData = twelveLeadData
        )
    }
    fun getCurrentTimestamp(): String {
        val currentMoment: Instant = Clock.System.now()
        return currentMoment.toEpochMilliseconds().toString()
    }
}