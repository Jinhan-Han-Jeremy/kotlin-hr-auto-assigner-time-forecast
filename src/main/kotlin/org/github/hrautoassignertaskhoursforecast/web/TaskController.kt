package org.github.hrautoassignertaskhoursforecast.web

import jakarta.validation.Valid
import org.github.hrautoassignertaskhoursforecast.Task.application.dto.TaskRequestDTO
import org.github.hrautoassignertaskhoursforecast.Task.application.dto.TaskResponseDTO
import org.github.hrautoassignertaskhoursforecast.Task.application.dto.TaskSearchDTO
import org.github.hrautoassignertaskhoursforecast.Task.application.service.TaskService
import org.github.hrautoassignertaskhoursforecast.global.RoleType
import org.github.hrautoassignertaskhoursforecast.global.exception.BadRequestException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/tasks")
class TaskController(private val taskService: TaskService) {

    @GetMapping
    suspend fun getAllTasks(): ResponseEntity<List<TaskResponseDTO>> {
        val tasks = taskService.findAllTasks()
        return ResponseEntity.ok(tasks)
    }

    @PostMapping
    suspend fun createTask(@Valid @RequestBody requestDTO: TaskRequestDTO): ResponseEntity<TaskResponseDTO> {
        val createdTask = taskService.createTask(requestDTO)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask)
    }

    @PutMapping("/{id}")
    suspend fun updateTask(
        @PathVariable id: Long,
        @Valid @RequestBody requestDTO: TaskRequestDTO
    ): ResponseEntity<TaskResponseDTO> {
        val updatedTask = taskService.updateTask(id, requestDTO)
        return ResponseEntity.ok(updatedTask)
    }

    @DeleteMapping("/{id}")
    suspend fun deleteTask(@PathVariable id: Long): ResponseEntity<Void> {
        taskService.deleteTask(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}")
    suspend fun getTaskById(@PathVariable id: Long): ResponseEntity<TaskResponseDTO> {
        val task = taskService.findTaskById(id)
        return ResponseEntity.ok(task)
    }

    @GetMapping("/search/{roleType}")
    suspend fun getTasksByRole(@PathVariable roleType: String): ResponseEntity<List<TaskResponseDTO>> {
        val role = RoleType.values()
            .find { it.displayName.contains(roleType, ignoreCase = true) }
            ?: throw BadRequestException("Invalid role type: $roleType")

        val tasks = taskService.findTasksByRole(role)
        return ResponseEntity.ok(tasks)
    }

    @GetMapping("/search")
    suspend fun searchTasks(@ModelAttribute inputData: TaskSearchDTO): ResponseEntity<List<TaskResponseDTO>> {
        val tasks = taskService.searchTasksByInput(inputData)
        return ResponseEntity.ok(tasks)
    }

    @GetMapping("/search/difficulty")
    suspend fun getTasksByDifficultyRange(@RequestParam level: Int): ResponseEntity<List<TaskResponseDTO>> {
        val tasks = taskService.findTasksByDifficulty(level)
        return ResponseEntity.ok(tasks)
    }
}