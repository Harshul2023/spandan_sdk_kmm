package com.example.spandansdkkmm.model

import `in`.sunfox.healthcare.java.commons.ecg_processor.conclusions.data.EcgData
import kotlinx.serialization.SerialName

/**
 * serialized name :- report_type,age,characteristics,conclusions,ecgData,
 * device_id,firmware_version,sdk_version,ecg_processor_version*/
data class SdkLogRequest(
    @SerialName("age")
    val age: String,
    val characteristics: Any,
    val conclusions: Any,
    @SerialName("ecg_data")
    val ecgData: EcgData,
    @SerialName("report_type")
    val reportType: String,
    @SerialName("device_id")
    val deviceId:String,
    @SerialName("firmware_version")
    val firmwareVersion:String,
    @SerialName("sdk_version")
    val sdkVersion:String,
    @SerialName("ecg_processor_version")
    val ecgProcessorVersion:String
)