//package com.example.spandansdkkmm.util
//
//import android.util.Base64
//import android.util.Log
//import com.auth0.android.jwt.JWT
//import `in`.sunfox.healthcare.commons.android.spandan_sdk.connection.SpandanSDKException
//import `in`.sunfox.healthcare.commons.android.spandan_sdk.enums.SpandanException
//import java.util.Random
//import javax.crypto.Cipher
//import javax.crypto.spec.SecretKeySpec
//
//object AuthenticationHelper {
//
//    private val TAG = "Authentication.TAG"
//
//
//    private fun decryptIV(strToDecrypt: String?, key: String): String {
//        try {
//            return decrypt(strToDecrypt!!, key)
//        } catch (e: Exception) {
//            throw SpandanSDKException("${SpandanException.InvalidTokenException} token is not valid.please check the token. ${e.message}")
//        }
////        return decrypt(strToDecrypt!!, key, )
//    }
//
//    private fun decrypt(strToDecrypt: String?, key: String): String {
//        val secretKey = SecretKeySpec(key.toByteArray(), "AES")
//        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
//        Log.d("Device.TAG", "decrypt: 23456789")
//        cipher.init(Cipher.DECRYPT_MODE, secretKey)
//        return String(cipher.doFinal(Base64.decode(strToDecrypt, Base64.URL_SAFE)))
//    }
//
//    fun isTokenExpired(token: String, sessionId: String?): Boolean {
//        val data = JWT(decryptIV(token, "ywggiq7836qppjki"))
////        return (data.claims["exp"]!!.asLong()!!.compareTo(System.currentTimeMillis()) != 1)
//        return !(data.claims["sid"]!!.asString().equals(sessionId, false))
//    }
//
//    /*fun isOfflineTokenExpired(token: String): Boolean {
//        val data = JWT(decryptIV(token, "ywggiq7836qppjki"))
//        return (data.claims["exp"]!!.asLong()!! < (System.currentTimeMillis()))
//    }*/
//
//    fun isOfflineTokenExpired(token: String): Boolean {
//        val data = JWT(decryptIV(token, "ywggiq7836qppjki"))
//        return try {
//            if (data.claims["exp"] != null) {
//                //for old token expiry check
//                (data.claims["exp"]!!.asLong()!! < (System.currentTimeMillis()))
//            } else {
//                //for new token expiry check
//                !data.claims["isOffLine"]!!.asBoolean()!!
//            }
//        } catch (e: Exception) {
//            !data.claims["isOffLine"]!!.asBoolean()!!
//        }
//    }
//
//    fun getListOfTestFromAuth(token: String): String? {
//        val data = JWT(decryptIV(token, "ywggiq7836qppjki"))
//        return data.claims["ta"]!!.asString()
//    }
//
//    fun getEnabledDeviceTypeFromAuth(token: String): String? {
//        try {
//            Log.d("Device.TAG", "getEnabledDeviceTypeFromAuth:")
//            val data = JWT(decryptIV(token, "ywggiq7836qppjki"))
//            Log.d("Device.TAG", "getEnabledDeviceTypeFromAuth: ${data.claims["de"]}")
//        }catch (e:Exception){
//            Log.d("Device.TAG", "getEnabledDeviceTypeFromAuth123456: ${e}")
//        }
//        val data = JWT(decryptIV(token, "ywggiq7836qppjki"))
////        return (data.claims["exp"]!!.asLong()!!.compareTo(System.currentTimeMillis()) != 1)
//        return (data.claims["de"]!!.asString())//for checking enabled device type(Neo, Pro or Legacy) from device
//    }
//
//    fun isUiEnabled(token: String): Boolean {
//        val data = JWT(decryptIV(token, "ywggiq7836qppjki"))
//        return data.claims["eUi"].let {
//            if (it != null)
//                it.asBoolean()!!
//            else
//                false
//        }
//    }
//
//    fun isGenerateReport(token: String): Boolean {
//        //this method is called to check if the client have access to the generate the report.
//        val data = JWT(decryptIV(token, "ywggiq7836qppjki"))
////        return data.claims["eG"].let {
////            if (it != null)
////                it.asBoolean()!!
////            else
////                false
////        }
//        return true
//    }
//
//    //    private fun encodeToBase64(stringToEncode:String):String{
////        val authKey = StringBuffer()
////        stringToEncode.forEach {
////            authKey.append(it.inc())
////        }
////        return String(Base64.encode(authKey.toString().toByteArray(),Base64.DEFAULT))
////    }
////
////    fun decodeAuthKey(authEncodedKey: String) : String {
////        val authKey = StringBuffer()
////        authEncodedKey.forEach {
////            authKey.append(it.dec())
////        }
////        return String(Base64.decode(authKey.toString(),Base64.DEFAULT))
////    }
//    fun createSessionId(): String {
//        val allowedCharacters = "0123456789qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM"
//        val sizeOfRandomString = 64
//        val random = Random()
//        val sessionId = StringBuilder(sizeOfRandomString)
//        for (i in 0 until sizeOfRandomString)
//            sessionId.append(allowedCharacters[random.nextInt(allowedCharacters.length)])
//        return sessionId.toString()
//    }
//}