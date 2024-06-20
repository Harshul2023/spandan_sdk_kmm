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
        decryptionKey = authenticationHelper().init(stringToHash)
    }


    /***
     * @param inputKey it is the combination of the id+created_at+master_key get from the generate token api.**/


    fun decrypt(
        strToDecrypt: String?
    ): String {
        return privateDecrypt(strToDecrypt!!, decryptionKey, iv)
    }

    private fun privateDecrypt(strToDecrypt: String?, key: String, iv: String): String {
        return authenticationHelper().decrypt(strToDecrypt,key,iv)
    }
}