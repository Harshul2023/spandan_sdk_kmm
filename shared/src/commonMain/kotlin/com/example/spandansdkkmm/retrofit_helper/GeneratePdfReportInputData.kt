package com.example.spandansdkkmm.retrofit_helper

import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName

data class GeneratePdfReportInputData(
    @SerialName("test_id")
    private val id:String = Clock.System.now().toEpochMilliseconds().toString()+ uuid4().toString().substring(0,4),

    @SerialName("patient_data")
    val patientData: PatientData,
    @SerialName("generate_pdf_report")
    val generatePdfReport: Boolean=true,
    @SerialName("processor_type")
    val processorType: String,
    @SerialName("meta_data")
    var metaData: MetaData = MetaData(),
    @SerialName("ecg_data")
    var ecgData: ApiEcgData = ApiEcgData()
)

data class PatientData(
    val age: String,
    @SerialName("first_name")
    val firstName: String,
    val height: String,
    val gender: String,
    @SerialName("last_name")
    val lastName: String,
    val weight: String,
)

data class ApiEcgData(
    @SerialName("lead1_data")
    val lead1Data: String="",
    @SerialName("lead2_data")
    val lead2Data: String="",
    @SerialName("v1_data")
    val v1Data: String="",
    @SerialName("v2_data")
    val v2Data: String="",
    @SerialName("v3_data")
    val v3Data: String="",
    @SerialName("v4_data")
    val v4Data: String="",
    @SerialName("v5_data")
    val v5Data: String="",
    @SerialName("v6_data")
    val v6Data: String="",
)

data class MetaData(
    @SerialName("report_id")
    val reportId:String = uuid4().toString().substring(0,4),
    @SerialName("device_variant")
    val deviceVariant:DeviceVariant=DeviceVariant.SPLG,
    @SerialName("device_id")
    val deviceId:String="",
    @SerialName("firmware_version")
    val firmwareVersion:String="",
    val source:String="Spandan SDK"
)

enum class DeviceVariant{
    SPLG,
    SPPR,
    SPNE
}