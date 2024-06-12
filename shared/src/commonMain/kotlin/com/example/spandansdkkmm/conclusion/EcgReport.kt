package com.example.spandansdkkmm.conclusion


import `in`.sunfox.healthcare.java.commons.ecg_processor.conclusions.characteristics.EcgCharacteristics
import `in`.sunfox.healthcare.java.commons.ecg_processor.conclusions.conclusion.EcgConclusion
import `in`.sunfox.healthcare.java.commons.ecg_processor.conclusions.data.EcgData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EcgReport(
    @SerialName("report_type") val reportType: String,
    @SerialName("report_timestamp") var timeStamp: String,
    @SerialName("ecg_data") var ecgData: EcgData? = null,
    @SerialName("characteristics") val ecgCharacteristics: EcgCharacteristics,
    @SerialName("conclusions") var conclusion: EcgConclusion,
    @SerialName("sdkVersion") var sdkVersion: String = "1.0.0"
)
