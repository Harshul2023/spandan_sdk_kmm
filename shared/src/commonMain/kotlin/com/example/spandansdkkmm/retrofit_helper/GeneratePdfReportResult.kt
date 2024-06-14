package com.example.spandansdkkmm.retrofit_helper


data class GeneratePdfReportResult(
    val `data`: ReportGenerationResult,
    val status: Int,
    val success: Boolean,
    val message:String
)