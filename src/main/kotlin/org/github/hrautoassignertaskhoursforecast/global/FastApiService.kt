package org.github.hrautoassignertaskhoursforecast.global

import org.github.hrautoassignertaskhoursforecast.global.exception.ServiceUnavailableException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class FastApiService(
    @Qualifier("fastApiWebClient") private val webClient: WebClient
) {
    fun getTaskNamesFromAnalyzedTexts(inputText: String): List<String> {
        try {
        return webClient.post()
            .uri("/analyze_workstream")  // baseUrl + "/analyze_workstream" => 최종: http://fast_api_text_analyzer:8000/analyze_workstream
            .bodyValue(mapOf("input_text" to inputText))
            .retrieve()
            .bodyToMono(List::class.java)
            .block()
            ?.filterIsInstance<String>()
            ?: throw ServiceUnavailableException("External API returned null")
            }
        catch (e: Exception) {
                throw ServiceUnavailableException("Failed to fetch external data: ${e.message}")
            }
   }
}