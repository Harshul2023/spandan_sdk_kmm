import com.example.spandansdkkmm.retrofit_helper.ReportGenerationResult

interface PDFReportGenerationCallback {
    fun onReportGenerationSuccess(reportGenerationResult: ReportGenerationResult)
    fun onReportGenerationFailed(errorMsg: String)
}