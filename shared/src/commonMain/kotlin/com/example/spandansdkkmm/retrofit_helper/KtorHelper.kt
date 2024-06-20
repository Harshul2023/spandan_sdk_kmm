import com.example.spandansdkkmm.httpClient
import com.example.spandansdkkmm.retrofit_helper.EcgTestLogResponse
import com.example.spandansdkkmm.retrofit_helper.GenerateAuthTokenResult
import com.example.spandansdkkmm.retrofit_helper.GeneratePdfReportInputData
import com.example.spandansdkkmm.retrofit_helper.GeneratePdfReportResult
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*

import io.ktor.util.InternalAPI
import io.ktor.utils.io.errors.IOException

import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json


class RetrofitHelper {

    companion object {
        private const val BASE_URL = "https://api.sunfox.in"
    }

    @OptIn(InternalAPI::class)
    fun getRetrofitInstance(): GenerateAuthTokenAPI {
        val client = httpClient()
//        val client = HttpClient(CIO) {
//            install(Logging) {
//                logger = Logger.DEFAULT
//                level = LogLevel.BODY
//            }
//            install(ContentNegotiation) {
//
//                json(Json {
//                    ignoreUnknownKeys = true
//                    isLenient = true
//                    encodeDefaults = true
//
//                })
//            }
////            install(JsonFeature) {
////                serializer = KotlinxSerializer(Json {
////                    ignoreUnknownKeys = true
////                    isLenient = true
////                    allowStructuredMapKeys = true
////                    prettyPrint = false
////                    useArrayPolymorphism = false
////                })
////            }
//            install(HttpTimeout) {
////                requestTimeoutMillis = 2 * 60 * 1000 // 2 minutes in milliseconds
//                requestTimeoutMillis = 15000L// 2 minutes in milliseconds
//            }
//
////            install(KotlinxSerializer) {
////                val jsonConfig = JsonConfiguration(encodeDefaults = true)
////                json = Json(jsonConfig)
////            }
//            defaultRequest {
//                contentType(ContentType.Application.Json)
//                accept(ContentType.Application.Json)
//            }
//            HttpResponseValidator {
//                validateResponse { response ->
//                    val statusCode = response.status.value
//                    if (statusCode !in 200..299) {
//                        throw ClientRequestException(response, response.status.description)
//                    }
//                }
//            }
//        }
        return object : GenerateAuthTokenAPI {
            //            override suspend fun pushEcgTestLog(
//                authorization: String?,
//                apiKey: String,
//                ecgTestLogData: EcgTestLogData
//            ): EcgTestLogResponse {
//                var response: HttpResponse? =null
//                try {
//                    response =
//                        client.post("https://api.sunfox.in/spandan-sdk/dev/v1/test-logs") {
//                            header("Authorization", authorization)
//                            header("api-key", apiKey)
//                            body = ecgTestLogData
//                        }
//                }catch (e:Exception)
//                {
//                    e.stackTraceToString();
//                }
//
//                val resultJson = response!!.body<EcgTestLogResponse>()
//                return resultJson
//
//            }
            override suspend fun pushEcgTestLog(
                authorization: String?,
                apiKey: String,
                ecgTestLogData: EcgTestLogData
            ): EcgTestLogResponse {
                return try {
                    val response =
                        client.post("https://api.sunfox.in/spandan-sdk/dev/v1/test-logs") {
                            setBody(body=ecgTestLogData)
                            header("Authorization", authorization)
                            header("api-key", apiKey)
                            contentType(ContentType.Application.Json)
//                            body = ecgTestLogData
                        }
                    if (response.status.isSuccess()) {
                        response.body<EcgTestLogResponse>()
                    } else {
                        throw IOException("Failed to fetch token: ${response.status}")
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                    throw IOException("Failed to fetch token: ${e.message}")
                }

            }

            override suspend fun getAuthToken(
                authorization: String,
                sessionId: String,
                apiKey: String
            ): GenerateAuthTokenResult {
                return try {
                    val response =
                        client.get("https://api.sunfox.in/spandan-sdk/dev/v1/auth/generate-token") {
                            header("Authorization", authorization)
                            header("sess-id", sessionId)
                            header("api-key", apiKey)
                        }
                    if (response.status.isSuccess()) {
                        response.body<GenerateAuthTokenResult>()
                    } else {
                        throw IOException("Failed to fetch token: ${response.status}")
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                    throw IOException("Failed to fetch token: ${e.message}")
                }
            }

            override suspend fun generatePdfReport(
                authorization: String?,
                apiKey: String,
                generatePdfReportInputData: GeneratePdfReportInputData
            ): GeneratePdfReportResult {
                val response = client.post("https://api.sunfox.in/ecg-processor/dev/v3/process") {
                    header("Authorization", authorization)
                    header("api-key", apiKey)
                    body = generatePdfReportInputData
                }
                val resultJson = response.body<GeneratePdfReportResult>()
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
        authorization: String?,
        apiKey: String,
        generatePdfReportInputData: GeneratePdfReportInputData
    ): GeneratePdfReportResult

    suspend fun pushEcgTestLog(
        authorization: String?,
        apiKey: String,
        ecgTestLogData: EcgTestLogData
    ): EcgTestLogResponse
}