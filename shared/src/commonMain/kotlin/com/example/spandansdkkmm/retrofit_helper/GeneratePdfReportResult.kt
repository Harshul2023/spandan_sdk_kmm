package com.example.spandansdkkmm.retrofit_helper

import kotlinx.serialization.Serializable


@Serializable
data class GeneratePdfReportResult(
    val data: ReportGenerationResult,
    val status: Int? = null,
    val success: Boolean,
    val message: String  = ""
)