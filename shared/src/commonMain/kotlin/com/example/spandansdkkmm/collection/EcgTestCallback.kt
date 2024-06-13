package com.example.spandansdkkmm.collection

import com.example.spandansdkkmm.enums.EcgPosition


interface EcgTestCallback {
    fun onTestFailed(statusCode: Int)
    fun onTestStarted(ecgPosition: EcgPosition)
    fun onElapsedTimeChanged(elapsedTime: Long, remainingTime: Long)
    fun onReceivedData(data: String)
    fun onPositionRecordingCompleted(ecgPosition: EcgPosition, ecgPoints: ArrayList<Double>?)
    fun onEcgTestCompleted(hashMap: HashMap<EcgPosition,ArrayList<Double>>)
}