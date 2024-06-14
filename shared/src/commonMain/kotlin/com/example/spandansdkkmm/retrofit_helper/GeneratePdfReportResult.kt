package com.example.spandansdkkmm.retrofit_helper

import ReportGenerationResult

data class GeneratePdfReportResult(
    val `data`: ReportGenerationResult,
    val status: Int,
    val success: Boolean,
    val message:String
)