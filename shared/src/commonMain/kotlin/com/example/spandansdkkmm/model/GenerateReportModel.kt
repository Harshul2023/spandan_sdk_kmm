import com.example.spandansdkkmm.enums.EcgTestType
import com.example.spandansdkkmm.retrofit_helper.PatientData
import kotlinx.serialization.SerialName

//package com.example.spandansdkkmm.model
//import com.google.gson.annotations.SerializedName
//import `in`.sunfox.healthcare.commons.android.spandan_sdk.retrofit_helper.ApiEcgData
//import `in`.sunfox.healthcare.commons.android.spandan_sdk.retrofit_helper.MetaData
//import `in`.sunfox.healthcare.commons.android.spandan_sdk.retrofit_helper.PatientData
//import kotlinx.serialization.SerialName
data class GenerateReportModel(
    @SerialName("patient_data")
    val patientData: PatientData,
    @SerialName("generate_pdf_report")
    val generatePdfReport: Boolean,
    @SerialName("processor_type")
    val processorType: EcgTestType
)