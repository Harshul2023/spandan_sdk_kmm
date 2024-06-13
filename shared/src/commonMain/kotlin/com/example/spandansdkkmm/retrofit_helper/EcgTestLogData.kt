
import kotlinx.serialization.SerialName

data class EcgTestLogData(
    val id: String = "",
    @SerialName("report_type")
    val reportType: String = "",
    @SerialName("firmware_version")
    val firmwareVersion: String = "",
    @SerialName("sdk_version")
    val sdkVersion: String = "",
)
