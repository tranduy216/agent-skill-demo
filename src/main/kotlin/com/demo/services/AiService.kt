package com.demo.services

import com.demo.models.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory

object AiService {

    private val logger = LoggerFactory.getLogger(AiService::class.java)
    private val httpClient = HttpClient(CIO)

    /**
     * Calls the LLM API to get proposed steps for a given query.
     * Supports OpenAI-compatible APIs.
     */
    suspend fun askLlm(
        query: String,
        modelName: String,
        apiKey: String,
        ragContext: List<RagDocumentDTO> = emptyList()
    ): String {
        val contextText = if (ragContext.isNotEmpty()) {
            "\n\nRelevant context from knowledge base:\n" +
                ragContext.joinToString("\n---\n") { "[${it.domain}] ${it.originalText}" }
        } else ""

        val systemPrompt = """You are an AI assistant. When asked to perform a task, propose clear numbered steps.
            |Format your response as:
            |PROPOSED STEPS:
            |1. [First step]
            |2. [Second step]
            |...
            |
            |EXPLANATION:
            |[Brief explanation of the approach]$contextText""".trimMargin()

        return try {
            val response = httpClient.post("https://api.openai.com/v1/chat/completions") {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("model", modelName)
                    putJsonArray("messages") {
                        addJsonObject {
                            put("role", "system")
                            put("content", systemPrompt)
                        }
                        addJsonObject {
                            put("role", "user")
                            put("content", query)
                        }
                    }
                    put("temperature", 0.7)
                    put("max_tokens", 1000)
                }.toString())
            }

            val body = response.bodyAsText()
            val json = Json.parseToJsonElement(body).jsonObject

            if (response.status == HttpStatusCode.OK) {
                json["choices"]?.jsonArray?.firstOrNull()
                    ?.jsonObject?.get("message")
                    ?.jsonObject?.get("content")
                    ?.jsonPrimitive?.content
                    ?: "No response from AI"
            } else {
                val error = json["error"]?.jsonObject?.get("message")?.jsonPrimitive?.content
                    ?: "Unknown API error (${response.status})"
                "API Error: $error"
            }
        } catch (e: Exception) {
            logger.error("LLM API call failed", e)
            generateFallbackResponse(query)
        }
    }

    /**
     * Fallback response when API is unavailable - generates a demo response.
     */
    private fun generateFallbackResponse(query: String): String {
        return """PROPOSED STEPS:
            |1. Analyze the request: "$query"
            |2. Research relevant information and best practices
            |3. Prepare a detailed implementation plan
            |4. Execute the plan with proper validation
            |5. Review results and provide summary
            |
            |EXPLANATION:
            |This is a structured approach to handle your request. Each step will be executed
            |sequentially after admin approval. The AI will analyze the query, gather context,
            |and propose specific actions for implementation.
            |
            |(Note: This is a demo response. Connect a real LLM API key for actual AI responses.)""".trimMargin()
    }

    fun extractSteps(aiResponse: String): List<String> {
        val lines = aiResponse.lines()
        return lines.filter { line ->
            line.trim().matches(Regex("^\\d+\\.\\s+.*"))
        }.map { it.trim() }
    }
}
