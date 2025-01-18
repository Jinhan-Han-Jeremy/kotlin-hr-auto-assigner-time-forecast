package org.github.hrautoassignertaskhoursforecast.web

import org.github.hrautoassignertaskhoursforecast.TaskHistory.application.TasksHistoryMapper
import org.github.hrautoassignertaskhoursforecast.TaskHistory.application.dto.TasksHistoryRequest
import org.github.hrautoassignertaskhoursforecast.TaskHistory.application.dto.TasksHistoryResponse

import org.github.hrautoassignertaskhoursforecast.TaskHistory.application.service.TasksHistoryService
import org.github.hrautoassignertaskhoursforecast.global.TaskState
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/tasks-history")
class TasksHistoryController(
    private val tasksHistoryService: TasksHistoryService,
    private val tasksHistoryMapper: TasksHistoryMapper
) {
    @PostMapping
    suspend fun createTasksHistory(@RequestBody request: TasksHistoryRequest): ResponseEntity<TasksHistoryResponse> {
        val entity = tasksHistoryMapper.toEntity(request)
        val savedEntity = tasksHistoryService.createTaskHistory(entity)
        val response = tasksHistoryMapper.toResponse(savedEntity)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    suspend fun getTasksHistory(@PathVariable id: Long): ResponseEntity<TasksHistoryResponse> {
        val entity = tasksHistoryService.findById(id)
        val response = tasksHistoryMapper.toResponse(entity)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    suspend fun deleteTasksHistory(@PathVariable id: Long): ResponseEntity<Void> {
        tasksHistoryService.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    suspend fun findAll(): ResponseEntity<List<TasksHistoryResponse>> {
        val tasks = tasksHistoryService.findAll()
        val response = tasks.map { tasksHistoryMapper.toResponse(it) }
        return ResponseEntity.ok(response)
    }

    @GetMapping("/search/requirements-satisfied")
    suspend fun findByRequirementsSatisfied(@RequestParam satisfied: Boolean): ResponseEntity<List<TasksHistoryResponse>> {
        val tasks = tasksHistoryService.findAllByRequirementsSatisfied(satisfied)
        val response = tasks.map { tasksHistoryMapper.toResponse(it) }
        return ResponseEntity.ok(response)
    }

    @GetMapping("/search/roles")
    suspend fun findByEmployeeRoles(@RequestParam role: String): ResponseEntity<List<TasksHistoryResponse>> {
        val tasks = tasksHistoryService.findAllByEmployeeRoles(role)
        val response = tasks.map { tasksHistoryMapper.toResponse(it) }
        return ResponseEntity.ok(response)
    }

    @GetMapping("/search/task-name")
    suspend fun findByName(@RequestParam name: String): ResponseEntity<List<TasksHistoryResponse>> {
        val tasks = tasksHistoryService.findAllByName(name)
        val response = tasks.map { tasksHistoryMapper.toResponse(it) }
        return ResponseEntity.ok(response)
    }

    @GetMapping("/search/less-spending-days")
    suspend fun findAllByLessSpendingDays(@RequestParam days: Float): ResponseEntity<List<TasksHistoryResponse>> {
        val tasks = tasksHistoryService.findAllByLessSpendingDays(days)
        val response = tasks.map { tasksHistoryMapper.toResponse(it) }
        return ResponseEntity.ok(response)
    }

    @GetMapping("/search/greater-spending-days")
    suspend fun findAllByGreaterSpendingDays(@RequestParam days: Float): ResponseEntity<List<TasksHistoryResponse>> {
        val tasks = tasksHistoryService.findAllByLongerThanSpendingDays(days)
        val response = tasks.map { tasksHistoryMapper.toResponse(it) }
        return ResponseEntity.ok(response)
    }

    @GetMapping("/search/spending-days")
    suspend fun findAllBySpendingDays(@RequestParam days: Float): ResponseEntity<List<TasksHistoryResponse>> {
        val tasks = tasksHistoryService.findAllBySpendingDays(days)
        val response = tasks.map { tasksHistoryMapper.toResponse(it) }
        return ResponseEntity.ok(response)
    }

    @GetMapping("/search/state")
    suspend fun findAllByState(@RequestParam state: String): ResponseEntity<List<TasksHistoryResponse>> {
        val taskState = TaskState.from(state)
        val tasks = tasksHistoryService.findAllByState(taskState)
        val response = tasks.map { tasksHistoryMapper.toResponse(it) }
        return ResponseEntity.ok(response)
    }

    @GetMapping("/search/date-range")
    suspend fun findTasksInRange(
        @RequestParam year: Int,
        @RequestParam month: Int,
        @RequestParam day: Int,
        @RequestParam untilYear: Int,
        @RequestParam untilMonth: Int,
        @RequestParam untilDay: Int
    ): ResponseEntity<List<TasksHistoryResponse>> {
        val startDate = LocalDate.of(year, month, day)
        val endDate = LocalDate.of(untilYear, untilMonth, untilDay)
        val tasks = tasksHistoryService.findByTimeInRange(startDate, endDate)
        val response = tasks.map { tasksHistoryMapper.toResponse(it) }
        return ResponseEntity.ok(response)
    }

    @GetMapping("/search/team-member-name")
    suspend fun findAllByMemberName(@RequestParam name: String): ResponseEntity<List<TasksHistoryResponse>> {
        val teamMember = tasksHistoryService.findAllByTeamMemberName(name)
        val response = teamMember.map { tasksHistoryMapper.toResponse(it) }
        return ResponseEntity.ok(response)
    }
}
