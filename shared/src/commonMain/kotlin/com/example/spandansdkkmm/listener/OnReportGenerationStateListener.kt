import com.example.spandansdkkmm.conclusion.EcgReport


interface OnReportGenerationStateListener {
    fun onReportGenerationSuccess(ecgReport: EcgReport)
    fun onReportGenerationFailed(errorCode: Int, errorMsg: String)
}