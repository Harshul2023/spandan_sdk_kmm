package com.example.spandansdkkmm
//
//import retrofit2.http.POST
object AppConfig {

    val API_ENDPOINT_GENERATE_TOKEN : String
        get() =
            when{
                BuildConfig.IS_DEBUG -> { "/spandan-sdk/dev/v1/auth/generate-token" }
                else -> { "/spandan-sdk/prod/v1/auth/generate-token" }
            }

    val API_ENDPOINT_GENERATE_PDF_REPORT : String
        get() =
            when{
                BuildConfig.IS_DEBUG -> { "/ecg-processor/dev/v3/process" }
                else -> {
                    "/ecg-processor/prod/v3/process"
                }
            }

    val API_ENDPOINT_LOG_TEST_DATA : String
        get() =
            when{
                BuildConfig.IS_DEBUG -> { "https://api.sunfox.in/spandan-sdk/dev/v1/test-logs" }
                else -> { "https://api.sunfox.in/spandan-sdk/prod/v1/test-logs" }
            }

    val MIX_PANEL_TOKEN : String
        get() =
            when{
                BuildConfig.IS_DEBUG -> { "8a2f0f059864b08b5e6226b7d47869da" }
                else -> { "9dfbe24f50ade465fc0d5bc98c7a786c" }
            }
}