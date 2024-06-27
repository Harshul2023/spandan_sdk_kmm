package com.example.spandansdkkmm.collection

import kotlinx.coroutines.*

open class CountDownTimer(
    private val duration: Long,
    private val interval: Long,
    private val onTick: (millisUntilFinished: Long) -> Unit,
    private val onFinish: () -> Unit
) {
    private var job: Job? = null
    private var remainingTime: Long = duration

    fun start() {
        // Cancel any existing job before starting a new one
        job?.cancel()

        // Reset remaining time
        remainingTime = duration

        job = CoroutineScope(Dispatchers.Default).launch {
            while (remainingTime > 0) {
                delay(interval)
                remainingTime -= interval
                withContext(Dispatchers.Main) {
                    onTick(remainingTime)
                }
            }
            withContext(Dispatchers.Main) {
                onFinish()
            }
        }
    }

    fun cancel() {
        job?.cancel()
        remainingTime = duration
    }
}
