//package com.example.spandansdkkmm.mixPanelHelper
//
//import android.content.Context
//import com.mixpanel.android.mpmetrics.MixpanelAPI
//import `in`.sunfox.healthcare.commons.android.spandan_sdk.AppConfig
//import org.json.JSONObject
//
//class MixPanelHelper {
//
//    private lateinit var mixpanelAPI: MixpanelAPI
//    companion object {
//
//        private val mixPanelHelper : MixPanelHelper?=null
//        fun getInstance(context:Context):MixPanelHelper{
//            return if (mixPanelHelper==null) {
//                val mixPanelHelper = MixPanelHelper()
//                mixPanelHelper.mixpanelAPI =
//                    MixpanelAPI.getInstance(context, AppConfig.mixPanelToken, false)
//                mixPanelHelper
//            } else
//                mixPanelHelper
//        }
//
//    }
//    fun sendToMixpanel(
//        eventName: String,
//        key: List<String>,
//        value: List<String>,
//    ) {
//        val props = JSONObject()
//        key.forEachIndexed { index, _key ->
//            props.put(_key, value[index])
//        }
////        props.put(key,value)
//        mixpanelAPI.track(eventName, props)
//    }
//
//    fun sendTimingEvent(
//        eventName: String,
//        key: List<String>,
//        value: List<String>,
//        startTimingEvent:Boolean
//    ){
//        val props = JSONObject()
//        key.forEachIndexed { index, _key ->
//            props.put(_key, value[index])
//        }
//        if (startTimingEvent)
//            mixpanelAPI.timeEvent(eventName)
//        else
//            mixpanelAPI.track(eventName,props)
//    }
//
//}