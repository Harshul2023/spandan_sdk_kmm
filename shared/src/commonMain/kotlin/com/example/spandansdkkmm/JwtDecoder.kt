package com.example.spandansdkkmm

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import io.ktor.util.decodeBase64String

object JwtDecoder {
    private val json = Json { ignoreUnknownKeys = true }

    fun decode(token: String): JsonObject {
        val parts = token.split(".")
        require(parts.size == 3) { "Invalid JWT token format" }

        val payloadJson = parts[1].decodeBase64String()
        return json.parseToJsonElement(payloadJson).jsonObject
    }
}