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

        return object : GenerateAuthTokenAPI {
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
                            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
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
                return try {
                    val response =
                        client.post("https://api.sunfox.in/ecg-processor/dev/v3/process") {
                            header("Authorization", authorization)
                            header("api-key", apiKey)
                            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                            setBody(generatePdfReportInputData)
                        }
                    if (response.status.isSuccess()) {
                        response.body<GeneratePdfReportResult>()
                    } else {
                        throw IOException("Failed to fetch report: ${response.status}")
                    }
                }
                 catch (e: Throwable) {
                e.printStackTrace()
                throw IOException("Failed to fetch report: ${e.message}")
            }
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