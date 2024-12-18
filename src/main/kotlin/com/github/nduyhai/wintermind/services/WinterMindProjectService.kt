package com.github.nduyhai.wintermind.services

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intellij.collaboration.util.withQuery
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Service(Service.Level.PROJECT)
class WinterMindProjectService(project: Project) {

    private val key: String = ""


    fun prompt(txt: String): String {
        val client = HttpClient.newHttpClient()
        val body = jacksonObjectMapper().writeValueAsString(GeminiRequest(txt))
        val request = HttpRequest.newBuilder()
            .uri(
                URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent")
                    .withQuery("key=$key")
            )
            .header("Content-Type", "application/json").POST(
                HttpRequest.BodyPublishers.ofString(
                    body
                )
            ).build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val mapper = jacksonObjectMapper()

        val geminiResponse = mapper.readValue(response.body(), GeminiResponse::class.java)

        return geminiResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: "No response"

    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeminiRequest(
    @JsonIgnore
    val text: String
) {
    val contents = listOf(
        GeminiRequestContent(
            parts = listOf(
                GeminiRequestPart(text)
            )
        )
    )
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeminiRequestContent(
    val parts: List<GeminiRequestPart>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeminiRequestPart(
    val text: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeminiResponse(
    val candidates: List<Candidate>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Candidate(
    val content: Content
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Content(
    val parts: List<Part>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Part(
    val text: String
)
