package com.example.spandansdkkmm
import android.util.Base64
import android.util.Log
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

 class AuthenticationHelperANDROID  {

    private var decryptionKey = ""
    private var iv = ""

    fun init(stringToHash: String): String {
        decryptionKey =
            MessageDigest.getInstance("SHA-256")
                .digest(stringToHash.toByteArray()).joinToString("") {
                    "%02x".format(it)
                }
                .substring(0, 32)
//        decryptionKey = "0cff84f3008c61285fa602dc6e154eac"
        return decryptionKey
    }

    fun decrypt(strToDecrypt: String?, key: String, iv: String): String {
        return privateDecrypt(strToDecrypt!!, key, iv)
    }

    private fun privateDecrypt(strToDecrypt: String?, key: String, iv: String): String {

        val secretKey = SecretKeySpec(key.toByteArray(), "AES")
        val ivData = IvParameterSpec(iv.toByteArray())

        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")

        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivData)

        val decodedBytes = Base64.decode(strToDecrypt, Base64.URL_SAFE)
        Log.d("Authentication",decodedBytes.size.toString())
        val decrypted = cipher.doFinal(decodedBytes)
        val str = String(decrypted)
        return String(decrypted)
    }
}
