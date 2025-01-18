package org.github.hrautoassignertaskhoursforecast.web

import org.github.hrautoassignertaskhoursforecast.WorkStream.application.dto.AnalyzedWorkStreamResponse
import org.github.hrautoassignertaskhoursforecast.WorkStream.application.dto.WorkStreamResponse
import org.github.hrautoassignertaskhoursforecast.WorkStream.application.service.WorkStreamService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/workstreams")
class WorkStreamController(private val workStreamService: WorkStreamService) {

    @GetMapping("/analyzingworkstream")
    suspend fun analyzingWorkStream(@RequestParam workInfo: String): ResponseEntity<AnalyzedWorkStreamResponse> {

        val response = workStreamService.analyzedWorkStream(workInfo)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    fun getWorkStreamById(@PathVariable id: Int): ResponseEntity<WorkStreamResponse> {
        val response = workStreamService.getWorkStreamById(id)
        return ResponseEntity.ok(response)
    }

    @GetMapping
    fun getAllWorkStreams(): ResponseEntity<List<WorkStreamResponse>> {
        val response = workStreamService.getAllWorkStreams()
        return ResponseEntity.ok(response)
    }
}