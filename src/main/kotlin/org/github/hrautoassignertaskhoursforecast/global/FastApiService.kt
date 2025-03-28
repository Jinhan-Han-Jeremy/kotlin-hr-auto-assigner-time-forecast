package org.github.hrautoassignertaskhoursforecast.global

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.github.hrautoassignertaskhoursforecast.global.exception.ServiceUnavailableException
import org.github.hrautoassignertaskhoursforecast.global.milp.MilpRequest
import org.github.hrautoassignertaskhoursforecast.global.milp.MilpResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class FastApiService(
    @Qualifier("fastApiWebClient") private val webClient: WebClient
) {
    private fun getTaskNamesFromAnalyzedTexts(inputText: String): List<String> {
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

    /** FastAPI를 호출하여 분석된 추천 Task 목록 받기
     */
    suspend fun recommendTasksByFastApi(workInfo: String): List<String> {
        val taskNames = getTaskNamesFromAnalyzedTexts(workInfo)
        println("Text 분석 추천 작업: $taskNames")
        return taskNames
    }

    private fun getMilpAnalyzedTaskAssignments(
        tasks: List<String>,
        memberPerformances: List<Map<String, Int>>
    ): MilpResponse {
        // 요청 데이터를 모델화
        val requestPayload = MilpRequest(tasks, memberPerformances)
        println("requestPayload from spring " +requestPayload)
        return try {
            webClient.post()
                .uri("/milp/execute")
                .contentType(MediaType.APPLICATION_JSON) // JSON 요청 명시
                .body(Mono.just(requestPayload), MilpRequest::class.java)  //JSON 직렬화 보장
                .retrieve()
                .bodyToMono(MilpResponse::class.java)
                .block() ?: throw ServiceUnavailableException("External API returned null")
        } catch (e: Exception) {
            throw ServiceUnavailableException("Failed to fetch external data: ${e.message}")
        }
    }

    suspend fun resultsAppliedByMilp(selectedMemberPerformances: List<Map<String, Int>>, selectedTaskNames: List<String>) : MilpResponse {
        val memberPerformances = selectedMemberPerformances

        return getMilpAnalyzedTaskAssignments(selectedTaskNames, memberPerformances)
    }


}