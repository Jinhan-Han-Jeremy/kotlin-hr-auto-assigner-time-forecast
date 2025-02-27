package org.github.hrautoassignertaskhoursforecast.task.infrastructure.jdbc

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.github.hrautoassignertaskhoursforecast.global.exception.ResourceNotFoundException
import org.github.hrautoassignertaskhoursforecast.task.domain.entity.Task
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Repository

@Repository
class TaskRepository(private val taskJpaRepository: TaskJpaRepository) {

    suspend fun findByTaskName(name: String): Task? = withContext(Dispatchers.IO) {
        taskJpaRepository.findByTaskName(name)
    }

    suspend fun findIdsByTaskNames(names: List<String>): List<Long> = withContext(Dispatchers.IO) {
        taskJpaRepository.findIdsByTaskNames(names)
    }

    suspend fun findNamesByIds(ids: List<Long>): List<String> = withContext(Dispatchers.IO) {
        taskJpaRepository.findNamesByIds(ids)
    }

    suspend fun findAllByDifficulty(difficulty: Int): List<Task> = withContext(Dispatchers.IO) {
        taskJpaRepository.findAllByDifficulty(difficulty)
    }

    suspend fun findAll(): List<Task> = withContext(Dispatchers.IO) {
        taskJpaRepository.findAll()
    }

    suspend fun findAllById(ids: List<Long>): List<Task> = withContext(Dispatchers.IO){
        taskJpaRepository.findAllById(ids)
    }

    suspend fun findById(id: Long): Task = withContext(Dispatchers.IO) {
        taskJpaRepository.findById(id).orElse(null) ?: throw ResourceNotFoundException("Task with ID $id not found")
    }

    suspend fun findAllBySpec(spec: Specification<Task>): List<Task> = withContext(Dispatchers.IO) {
        taskJpaRepository.findAll(spec)
    }

    suspend fun save(task: Task): Task = withContext(Dispatchers.IO) {
        taskJpaRepository.save(task)  //비동기적으로 데이터 저장
    }

    suspend fun delete(task: Task) = withContext(Dispatchers.IO) {
        taskJpaRepository.delete(task)  // 비동기적으로 데이터 삭제
    }

}