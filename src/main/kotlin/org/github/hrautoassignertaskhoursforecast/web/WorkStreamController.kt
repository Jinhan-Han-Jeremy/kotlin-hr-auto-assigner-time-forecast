package org.github.hrautoassignertaskhoursforecast.web

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.github.hrautoassignertaskhoursforecast.Task.application.dto.TaskResponseDTO
import org.github.hrautoassignertaskhoursforecast.WorkStream.application.dto.AnalyzedWorkStreamResponse
import org.github.hrautoassignertaskhoursforecast.WorkStream.application.dto.WorkStreamResponse
import org.github.hrautoassignertaskhoursforecast.WorkStream.application.service.WorkStreamService
import org.github.hrautoassignertaskhoursforecast.global.exception.BadRequestException
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/workstreams")
class WorkStreamController(private val workStreamService: WorkStreamService) {

    private val log = LoggerFactory.getLogger(WorkStreamController::class.java)

    @GetMapping
    suspend fun getAllTasks(): ResponseEntity<List<WorkStreamResponse>> {
        val workStreams = workStreamService.findAllWorkStream()
        return ResponseEntity.ok(workStreams)
    }

    @GetMapping("/analyzingworkstream")
    suspend fun analyzingWorkStream(@RequestParam workInfo: String): ResponseEntity<AnalyzedWorkStreamResponse> {

        val response = workStreamService.analyzedWorkStream(workInfo)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    suspend fun getWorkStreamById(@PathVariable id: Int): ResponseEntity<WorkStreamResponse> {
        val response = workStreamService.getWorkStreamById(id)
        return ResponseEntity.ok(response)
    }

    @GetMapping
    suspend fun getAllWorkStreams(): ResponseEntity<List<WorkStreamResponse>> {
        val response = workStreamService.getAllWorkStreams()
        return ResponseEntity.ok(response)
    }

    @PostMapping("/assign")
    suspend fun assignTasksAndWorkInfo(
        @RequestParam workInfo: String,
        @RequestParam firstId: Long,
        @RequestParam secondId: Long,
        @RequestParam thirdId: Long
    ): ResponseEntity<WorkStreamResponse> {
        // 컨트롤러에서는 예외 발생 시 바로 throw

        val idList = listOf(firstId, secondId, thirdId)
        print("id list : "+ idList)

        if (workInfo.isBlank()) {
            throw BadRequestException("workInfo cannot be blank.")
        }

        val response = workStreamService.tasksAndWorkInfoAssignInTasksHistory(workInfo, idList)
        return ResponseEntity.ok(response)
    }
}