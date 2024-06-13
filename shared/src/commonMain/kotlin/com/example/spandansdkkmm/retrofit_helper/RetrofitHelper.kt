//class RetrofitHelper {
//
//
//    companion object {
//        private const val BASE_URL = "https://api.sunfox.in"
//    }
//
//
//    fun getRetrofitInstance(): GenerateAuthTokenAPI {
//        val interceptor = HttpLoggingInterceptor()
//        interceptor.level = HttpLoggingInterceptor.Level.BODY
//        val client = OkHttpClient.Builder()
//            .connectTimeout(2, TimeUnit.MINUTES)
//            .readTimeout(2, TimeUnit.MINUTES)
//            .writeTimeout(2, TimeUnit.MINUTES)
//            .addInterceptor(interceptor)
////            .addInterceptor(Interceptor { chain ->
////                val original: Request = chain.request()
////                val request: Request = original.newBuilder()
////                    .headers(original.headers)
////                    .method(original.method,original.body)
////                    .build()
////
////                val response = chain.proceed(request)
////                if (response.code != 200 && response.code != 201) {
////                    // Magic is here ( Handle the error as your way )
////                    throw Exception()
////                }
////                response
////            })
//            .build()
//        val retrofit = Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .client(client)
//            .addConverterFactory(
//                GsonConverterFactory.create(
//                    GsonBuilder()
//                        .setLenient()
//                        .create()
//                )
//            )
//            .build()
//        return retrofit.create(GenerateAuthTokenAPI::class.java)
//    }
//}
//
//
//interface GenerateAuthTokenAPI {
//    /**
//     * @param authorization value is the verifierToken according to initial integrations.*/
//    @GET
//    fun getAuthToken(
//        @Url generateAuthTokenApiEndpoint: String = AppConfig.API_ENDPOINT_GENERATE_TOKEN,
//        @Header("Authorization") authorization: String,
//        @Header("sess-id") sessionId: String,
//        @Header("api-key") apiKey: String,
//    ): Call<GenerateAuthTokenResult>
//
//    /**
//     * @param apiKey value is the verifierToken according to the initial integrations.*/
//    @POST
//    fun generatePdfReport(
//        @Url generatePdfReportUrl: String = AppConfig.API_ENDPOINT_GENERATE_PDF_REPORT,
//        @Header("Authorization") authorization: String,
//        @Header("api-key") apiKey: String,
//        @Body generatePdfReportInputData: GeneratePdfReportInputData,
//    ): Call<GeneratePdfReportResult>
//
//    @POST
//    fun pushEcgTestLog(
//        @Url ecgTestLogEndpoint: String = AppConfig.API_ENDPOINT_LOG_TEST_DATA,
//        @Header("Authorization") authorization: String,
//        @Header("api-key") apiKey: String,
//        @Body ecgTestLogData: EcgTestLogData,
//    ): Call<EcgTestLogResponse>
//}