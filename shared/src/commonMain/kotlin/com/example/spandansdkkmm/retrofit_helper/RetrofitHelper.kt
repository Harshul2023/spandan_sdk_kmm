//package com.example.spandansdkkmm.retrofit_helper
//
//import com.google.gson.GsonBuilder
//import com.google.gson.annotations.SerializedName
//import `in`.sunfox.healthcare.commons.android.spandan_sdk.AppConfig
//import okhttp3.OkHttpClient
//import `in`.sunfox.healthcare.commons.android.spandan_sdk.model.SdkLogRequest
//import kotlinx.serialization.SerialName
//import okhttp3.ResponseBody
//import okhttp3.logging.HttpLoggingInterceptor
//import retrofit2.Call
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import retrofit2.http.Body
//import retrofit2.http.GET
//import retrofit2.http.Header
//import retrofit2.http.Url
//import java.util.concurrent.TimeUnit
//import retrofit2.http.POST
//
//
//class RetrofitHelper {
//    fun getRetrofitInstance(): TokenRefreshApi {
//
//        val interceptor = HttpLoggingInterceptor()
//        interceptor.level = HttpLoggingInterceptor.Level.BODY
//        val client = OkHttpClient.Builder()
//            .connectTimeout(2, TimeUnit.MINUTES)
//            .readTimeout(2, TimeUnit.MINUTES)
//            .writeTimeout(2, TimeUnit.MINUTES)
//            .addInterceptor(interceptor)
//            .build()
//        val retrofit = Retrofit.Builder()
//            .baseUrl("https://api.sunfox.in")
////            .addConverterFactory(GsonConverterFactory.create())
//            .client(client)
//            .addConverterFactory(
//                GsonConverterFactory.create(
//                    GsonBuilder()
//                        .setLenient()
//                        .create()
//                )
//            )
//            .build()
//        return retrofit.create(TokenRefreshApi::class.java)
//    }
//
//    fun logGenerateReportEvent(): TokenRefreshApi {
//        val retrofit = Retrofit.Builder()
//            .baseUrl("https://qkfr4sj2zh.execute-api.ap-south-1.amazonaws.com")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//        return retrofit.create(TokenRefreshApi::class.java)
//    }
//}
//
//class TokenRefreshResult(
//    @SerialName("message")
//    var message: String,
//    @SerialName("token")
//    var token: String,
//)
//
//
//interface TokenRefreshApi {
//    //    @GET("/v2/spandan/token-refresh/")
////    @GET("/spandan-sdk/prod/v1/auth/generate-token")
////    @GET("/spandan-sdk/dev/v1/auth/generate-token")
//    @GET
//    fun getToken(
//        @Url tokenRefreshUrl: String = AppConfig.tokenRefreshLink,
//        @Header("Authorization") verifierToken: String,
//        @Header("sess-id") sessionId: String,
//        @Header("api-key") apiKey: String,
//    ): Call<TokenRefreshResult>
//
//
//    //    @POST("/spandan-sdk/dev/v1/tests")
////    @POST("/spandan-sdk/prod/v1/tests")
////    fun saveLogs(
////        @Url logUrl: String = AppConfig.logUrl,
////        @Header("Authorization") verifier_token: String,
////        @Header("api-key") api_key: String, @Body ecgReport: SdkLogRequest,
////    ): Call<LogResponse>
//
//    //    @POST("/ecg-processor/dev/v3/process/")
////    @POST("/ecg-processor/prod/v3/process/")
//    @POST
//    fun generateReport(
//        @Url generateReportUrl: String = AppConfig.reportGenerationLink,
//        @Header("Authorization") authorization: String,
//        @Header("api-key") verifierToken: String,
//        @Body ecgReportApiInput: EcgReportApiInput,
//    ): Call<GenerateReportResult>
//
//    @POST
//    fun pushTestCompleteLog(
//        @Url testCompleteLog: String = AppConfig.testCompleteLog,
//        @Header("Authorization") authorization : String,
//        @Header("api-key") apiKey : String,
//        @Body testCompleteLogBody: TestCompleteLogBody
//    ):Call<LogResponse>
//}
//
//data class TestCompleteLogBody(
//    val id: String = "",
//    @SerialName("report_type")
//    val reportType: String = "",
//    @SerialName("firmware_version")
//    val firmwareVersion: String = "",
//    @SerialName("sdk_version")
//    val sdkVersion: String = "",
//)
//
//class LogResponse(
//    @SerialName("message")
//    var message: String,
//)