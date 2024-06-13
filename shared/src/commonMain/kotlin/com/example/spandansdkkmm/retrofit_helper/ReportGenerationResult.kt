package com.example.spandansdkkmm.retrofit_helper

import kotlinx.serialization.SerialName

data class ReportGenerationResult(
    val characteristics: Characteristics,
    val conclusions: Conclusions,
    @SerialName("url")
    val pdfReportUrl: String
)