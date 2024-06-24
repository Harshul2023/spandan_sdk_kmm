import cocoapods.SericomPod.SeriCom
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray
import kotlinx.cinterop.*
import platform.CoreCrypto.*
import platform.Foundation.*
import platform.posix.size_t
import platform.posix.size_tVar


class AuthenticationHelperIOS {
    private val TAG = "Authentication.TAG"
    private var decryptionKey = ""
    private var iv = ""

    @OptIn(ExperimentalForeignApi::class)
    fun init(stringToHash: String): String {

        decryptionKey = SeriCom.getsha256WithData(stringToHash)
//        decryptionKey = "0cff84f3008c61285fa602dc6e154eac"
        println("Decryption Key: $decryptionKey")
        return decryptionKey
    }

    @OptIn(ExperimentalForeignApi::class)
    public fun decrypt(strToDecrypt: String?, key: String, iv: String): String {

        print(strToDecrypt)
//        val s = SeriCom.decryptAESWithKeyStr(keyStr = key, ivStr = iv,str = strToDecrypt!!)
        val s = SeriCom.decryptWithKey(key,iv, strToDecrypt.toString())
        print(s)
        return s!!
    }


}
