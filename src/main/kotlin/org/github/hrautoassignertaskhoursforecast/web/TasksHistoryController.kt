package org.github.hrautoassignertaskhoursforecast.web

import org.github.hrautoassignertaskhoursforecast.taskHistory.application.TasksHistoryMapper
import org.github.hrautoassignertaskhoursforecast.taskHistory.application.dto.TasksHistoryRequest
import org.github.hrautoassignertaskhoursforecast.taskHistory.application.dto.TasksHistoryResponse
import org.github.hrautoassignertaskhoursforecast.taskHistory.application.service.TasksHistoryManipulator

import org.github.hrautoassignertaskhoursforecast.taskHistory.application.service.TasksHistoryService
import org.github.hrautoassignertaskhoursforecast.global.TaskState
import org.github.hrautoassignertaskhoursforecast.teamMembers.application.service.TeamMemberRankingService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/tasks-history")
class TasksHistoryController(
    private val tasksHistoryService: TasksHistoryService,
    private val tasksHistoryManipulator: TasksHistoryManipulator,
    private val tasksHistoryMapper: TasksHistoryMapper,
    private val teamMemberRankingService: TeamMemberRankingService
) {
    @GetMapping
    suspend fun findAll(): ResponseEntity<List<TasksHistoryResponse>> {
        val tasks = tasksHistoryService.findAll()
        val response = tasks.map { tasksHistoryMapper.toResponse(it) }
        return ResponseEntity.ok(response)
    }

    @PostMapping
    suspend fun createTasksHistory(@RequestBody request: TasksHistoryRequest): ResponseEntity<TasksHistoryResponse> {
        val savedEntity = tasksHistoryService.createTaskHistory(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEntity)
    }

    @PutMapping
    suspend fun updateAchievementsScore(
        @RequestParam historyName: String      // 존재하는 히스토리 이름
    ): ResponseEntity<String> {
        return try {
            // 점수 업데이트 로직 호출
            teamMemberRankingService.updateTeamMemberAchievementsScore(historyName)

            // 성공 메시지 반환
            ResponseEntity.ok("Achievements scores updated successfully.")
        } catch (e: Exception) {
            // 예외 처리 및 에러 메시지 반환
            ResponseEntity.badRequest().body("Failed to update achievements scores: ${e.message}")
        }
    }

    @PatchMapping("/update-state")
    suspend fun patchTasksHistoryStateAndEndDate(
        @RequestParam historyName: String,
        @RequestParam(required = false) state: TaskState?,
        @RequestParam(required = false) endedAt: LocalDate?
    ): ResponseEntity<String> {
        return try {
            val updated = tasksHistoryService. updateStateAndEndDate(historyName, state, endedAt)
            if (updated) {
                ResponseEntity.ok("Task history partially updated successfully.")
            } else {
                ResponseEntity.badRequest().body("Task history not found.")
            }
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body("Error updating task history: ${e.message}")
        }
    }

    @DeleteMapping("/{id}")
    suspend fun deleteTasksHistory(@PathVariable id: Long): ResponseEntity<Void> {
        tasksHistoryService.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}")
    suspend fun getTasksHistory(@PathVariable id: Long): ResponseEntity<TasksHistoryResponse> {
        val entity = tasksHistoryService.findById(id)
        val response = tasksHistoryMapper.toResponse(entity)
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
