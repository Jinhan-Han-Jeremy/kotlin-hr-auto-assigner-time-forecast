package org.github.hrautoassignertaskhoursforecast.web
import org.github.hrautoassignertaskhoursforecast.analyzingCalculator.application.dto.analyzedResultsResponse
import org.github.hrautoassignertaskhoursforecast.analyzingCalculator.application.service.TaskTimeForecastService
import org.github.hrautoassignertaskhoursforecast.global.TaskState
import org.github.hrautoassignertaskhoursforecast.taskHistory.application.dto.TasksHistoryResponse
import org.github.hrautoassignertaskhoursforecast.taskHistory.application.service.TasksHistoryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/forecast-tasks-members")
class AnalyzindCalculatorController (
    private val forecastService: TaskTimeForecastService, // 서비스 클래스 주입 (DI)
    private val tasksHistoryService: TasksHistoryService
) {

    @GetMapping("/forecast")
    suspend fun getForecastResults(
        @RequestParam selectedTaskNames: List<String>,
        @RequestParam NonfilterByLevel: Boolean
    ): ResponseEntity<analyzedResultsResponse> {
        val response = forecastService.analyzeForecast(selectedTaskNames, NonfilterByLevel)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/automated-forecast")
    suspend fun getAutomatedForecastResults(
        @RequestParam NonfilterByLevel: Boolean
    ): ResponseEntity<analyzedResultsResponse> {

        val tasksHistories = tasksHistoryService.findAllByRequirementsSatisfied(true)

        // TaskState.NOT_STARTED 상태인 항목들의 name 추출
        val tasksHistoriesNotStarted = tasksHistories
            .filter { it.state == TaskState.NOT_STARTED }  // 조건 필터링

        val response: analyzedResultsResponse

        if(tasksHistoriesNotStarted.isNotEmpty()) {
            val selectedTaskNames = tasksHistoriesNotStarted.map { it.name }

            //어려운 작업 별로 최대한 많은 인원 배치 greed와 비슷한 형태로 할당
            val results = forecastService.resultsPriortizedByDifficulty(selectedTaskNames, NonfilterByLevel)

            print("forecastService results " + results)

            // MILP 분석 결과
            val milpResults = forecastService.milpToAnalyzedResults(selectedTaskNames, NonfilterByLevel)

            // analyzedResultsResponse 객체를 생성하여 응답
            response = analyzedResultsResponse(
                resultsPriortizedByDifficulty = results,
                milpToAnalyzedResults = milpResults
            )
        }
        else{
            response = analyzedResultsResponse(
                resultsPriortizedByDifficulty = emptyList(),
                milpToAnalyzedResults = emptyList()
            )
        }

        return ResponseEntity.ok(response)
    }

    @PostMapping("/assign-expected-task-histories")
    suspend fun updateOrCreateTaskHistories(
        @RequestParam selectedTaskNames: List<String>,
        @RequestParam assignmentMethod: Int,
        @RequestParam nonFilterByLevel: Boolean,
        @RequestParam(required = false) startedAt: LocalDate?
    ): ResponseEntity<List<TasksHistoryResponse>> {

        val response: List<TasksHistoryResponse> = forecastService.updateOrCreateTaskHistory(
            selectedTaskNames,
            assignmentMethod,
            nonFilterByLevel,
            startedAt
        )

        return ResponseEntity.ok(response)
    }

}