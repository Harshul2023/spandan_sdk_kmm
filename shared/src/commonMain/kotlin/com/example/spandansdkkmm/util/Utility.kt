package com.example.spandansdkkmm.util

import com.example.spandansdkkmm.enums.SpandanDeviceVariant
import com.example.spandansdkkmm.retrofit_helper.DeviceVariant


object Utility {

    fun mapVariant(deviceVariant: SpandanDeviceVariant): String {
        return when(deviceVariant){
            SpandanDeviceVariant.SPANDAN_LEGACY -> "splg"
            SpandanDeviceVariant.SPANDAN_PRO -> "sppr"
            SpandanDeviceVariant.SPANDAN_NEO -> "spne"
            else -> "splg"
        }
    }

    fun mapSpandanVariantToDeviceVariant(spandanDeviceVariant: SpandanDeviceVariant): DeviceVariant {
        return when(spandanDeviceVariant){
            SpandanDeviceVariant.SPANDAN_PRO -> DeviceVariant.SPPR
            SpandanDeviceVariant.SPANDAN_NEO -> DeviceVariant.SPNE
            SpandanDeviceVariant.SPANDAN_LEGACY -> DeviceVariant.SPLG
        }
    }

}