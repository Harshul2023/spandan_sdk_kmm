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

    fun init(stringToHash: String): String {
        decryptionKey = sha256(stringToHash).substring(0, 32)
        decryptionKey = "0cff84f3008c61285fa602dc6e154eac"
        println("Decryption Key: $decryptionKey")
        return decryptionKey
    }

    fun decrypt(strToDecrypt: String?, key: String, iv: String): String {
        return privateDecrypt(strToDecrypt!!, key, iv)
    }


    @OptIn(ExperimentalForeignApi::class)
    private fun privateDecrypt(strToDecrypt: String?, key: String, iv: String): String {
       val ss= SeriCom.hey()
        print(ss)
        return ss!!
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun sha256(input: String): String {
        val inputData = input.toByteArray()
        val hash = UByteArray(CC_SHA256_DIGEST_LENGTH)
        inputData.usePinned { pinnedInput ->
            hash.usePinned { pinnedHash ->
                CC_SHA256(pinnedInput.addressOf(0), inputData.size.convert(), pinnedHash.addressOf(0))
            }
        }
        val hexChars = "0123456789abcdef"
        val hexString = StringBuilder(2 * CC_SHA256_DIGEST_LENGTH)
        for (byte in hash) {
            hexString.append(hexChars[(byte.toInt() and 0xF0) shr 4])
            hexString.append(hexChars[(byte.toInt() and 0x0F)])
        }
        println("SHA-256 Hash: $hexString")
        return hexString.toString()
    }
}
