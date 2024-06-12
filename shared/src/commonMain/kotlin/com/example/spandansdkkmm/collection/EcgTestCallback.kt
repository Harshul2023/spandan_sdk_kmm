package com.example.spandansdkkmm.collection

import com.example.spandansdkkmm.enums.EcgPosition


interface EcgTestCallback {
    //    fun onTestComplete(ecgData: ArrayList<Double>?)
    fun onTestFailed(statusCode: Int)
    fun onTestStarted(ecgPosition: EcgPosition)
    fun onElapsedTimeChanged(elapsedTime: Long, remainingTime: Long)
    fun onReceivedData(data: String)
    fun onPositionRecordingComplete(ecgPosition: EcgPosition, ecgPoints: ArrayList<Double>?)
    fun ecgTestCompleted(hashMap: HashMap<EcgPosition,ArrayList<Double>>)
}