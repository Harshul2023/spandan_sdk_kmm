
import com.example.spandansdkkmm.enums.SpandanDeviceVariant



data class DeviceInfo(
    var deviceVariant : SpandanDeviceVariant = SpandanDeviceVariant.SPANDAN_LEGACY,
    var deviceId : String? = null,
    var deviceMId : String? = null,
    var firmwareVersion : String? = null
)