package com.example.spandansdkkmm.collection

import com.example.spandansdkkmm.enums.EcgPosition


interface OnPositionRecordingCompleteListener {
    fun onPositionRecordingComplete(ecgPosition: EcgPosition)
}