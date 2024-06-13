import com.example.spandansdkkmm.authenticationHelper

class AuthenticationHelper(
    id: String,
    createdAt: String,
    masterKey: String
) {

    private val TAG = "Authentication.TAG"
    private var decryptionKey = ""
    private var iv = ""

    init {
        iv = id.substring(0, 16)
        val stringToHash = id + createdAt + masterKey
//        decryptionKey =
//            MessageDigest.getInstance("SHA-256")
//                .digest(stringToHash.toByteArray()).joinToString("")
//                {
//                    "%02x".format(it)
//                }
//                .substring(0, 32)
        decryptionKey = authenticationHelper().init(stringToHash)
    }


    /***
     * @param inputKey it is the combination of the id+created_at+master_key get from the generate token api.**/
//    fun initialiseDecryptionKey(inputKey: String, iv: String) {
//        this.iv = iv
//        decryptionKey =
//            MessageDigest.getInstance("SHA-256")
//                .digest(inputKey.toByteArray()).joinToString("")
//                {
//                    "%02x".format(it)
//                }
//                .substring(0, 32)
//    }

    fun decrypt(
        strToDecrypt: String?
    ): String {
        return privateDecrypt(strToDecrypt!!, decryptionKey, iv)
    }

    private fun privateDecrypt(strToDecrypt: String?, key: String, iv: String): String {
//        val secretKey = SecretKeySpec(key.toByteArray(), "AES")
//        val ivData = IvParameterSpec(iv.toByteArray())
//        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
//        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivData)
//        val decodedBytes = Base64.decode(strToDecrypt, Base64.URL_SAFE)
//        val decrypted = cipher.doFinal(decodedBytes)
//        return String(decrypted)
        return authenticationHelper().decrypt(strToDecrypt,key,iv)
    }
}