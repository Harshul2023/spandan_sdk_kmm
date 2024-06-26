package com.example.spandansdkkmm.retrofit_helper

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReportGenerationResult(
    val characteristics: Characteristics,
    val conclusions: Conclusions,
    @SerialName("url")
    val pdfReportUrl: String
)