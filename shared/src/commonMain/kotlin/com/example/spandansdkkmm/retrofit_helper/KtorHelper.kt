import com.example.spandansdkkmm.retrofit_helper.EcgTestLogResponse
import com.example.spandansdkkmm.retrofit_helper.GenerateAuthTokenResult
import com.example.spandansdkkmm.retrofit_helper.GeneratePdfReportInputData
import com.example.spandansdkkmm.retrofit_helper.GeneratePdfReportResult
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.call.receive
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.kotlinx.serializer.KotlinxSerializer
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import io.ktor.util.InternalAPI
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

class RetrofitHelper {

    companion object {
        private const val BASE_URL = "https://api.sunfox.in"
    }

    @OptIn(InternalAPI::class)
    fun getRetrofitInstance(): GenerateAuthTokenAPI {
        val client = HttpClient(CIO) {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.BODY
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 2 * 60 * 1000 // 2 minutes in milliseconds
            }
//            install(KotlinxSerializer) {
//                val jsonConfig = JsonConfiguration(encodeDefaults = true)
//                json = Json(jsonConfig)
//            }
            defaultRequest {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }
            HttpResponseValidator {
                validateResponse { response ->
                    val statusCode = response.status.value
                    if (statusCode !in 200..299) {
                        throw ClientRequestException(response, response.status.description)
                    }
                }
            }
        }
        return object : GenerateAuthTokenAPI {
            override suspend fun getAuthToken(
                authorization: String,
                sessionId: String,
                apiKey: String
            ): GenerateAuthTokenResult {
                val response= client.get("https://api.sunfox.in/auth/token") {
                    header("Authorization", authorization)
                    header("sess-id", sessionId)
                    header("api-key", apiKey)
                }
                val resultJson = response.body<GenerateAuthTokenResult>()
                return resultJson
            }

            override  suspend fun generatePdfReport(
                authorization: String,
                apiKey: String,
                generatePdfReportInputData: GeneratePdfReportInputData
            ): GeneratePdfReportResult {
                val response = client.post("https://api.sunfox.in/generate-pdf-report") {
                    header("Authorization", authorization)
                    header("api-key", apiKey)
                    body = generatePdfReportInputData
                }
                val resultJson = response.body<GeneratePdfReportResult>()
                return resultJson

            }

            override suspend fun pushEcgTestLog(
                authorization: String,
                apiKey: String,
                ecgTestLogData: EcgTestLogData
            ): EcgTestLogResponse {
                val response = client.post("https://api.sunfox.in/log-test-data") {
                    header("Authorization", authorization)
                    header("api-key", apiKey)
                    body = ecgTestLogData
                }

                val resultJson = response.body<EcgTestLogResponse>()
                return resultJson
            }
        }
    }

}

interface GenerateAuthTokenAPI {
    suspend fun getAuthToken(
        authorization: String,
        sessionId: String,
        apiKey: String
    ): GenerateAuthTokenResult

    suspend fun generatePdfReport(
        authorization: String,
        apiKey: String,
        generatePdfReportInputData: GeneratePdfReportInputData
    ): GeneratePdfReportResult

    suspend fun pushEcgTestLog(
        authorization: String,
        apiKey: String,
        ecgTestLogData: EcgTestLogData
    ): EcgTestLogResponse
}