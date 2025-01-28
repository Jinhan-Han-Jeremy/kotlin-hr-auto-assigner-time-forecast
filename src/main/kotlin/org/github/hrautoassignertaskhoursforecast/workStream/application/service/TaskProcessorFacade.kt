package org.github.hrautoassignertaskhoursforecast.workStream.application.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.github.hrautoassignertaskhoursforecast.task.infrastructure.jdbc.TaskRepository
import org.github.hrautoassignertaskhoursforecast.task.domain.entity.Task
import org.github.hrautoassignertaskhoursforecast.taskHistory.application.dto.TasksHistoryRequest
import org.github.hrautoassignertaskhoursforecast.taskHistory.application.dto.TasksHistoryResponse
import org.github.hrautoassignertaskhoursforecast.taskHistory.application.service.TasksHistoryService
import org.github.hrautoassignertaskhoursforecast.taskHistory.infrastructure.jdbc.TasksHistoryRepository
import org.github.hrautoassignertaskhoursforecast.utility.Trie.TrieRecommender
import org.springframework.stereotype.Service

@Service
class TaskProcessorFacade(
    private val tasksRepository: TaskRepository,
    private val tasksHistoryRepository: TasksHistoryRepository,
    private val tasksHistoryService: TasksHistoryService,
    private val trieRecommender: TrieRecommender // 새 의존성 추가
) {

    fun getTasksByIds(ids: List<Long>): List<Task>
    = tasksRepository.findAllById(ids)

    fun getTaskById(id: Long): Task
    = tasksRepository.findById(id).get()

    fun getNamesByIds(ids: List<Long>): List<String>
    = tasksRepository.findNamesByIds(ids)

    fun getIdsByTaskNames(names: List<String>): List<Long>
    = tasksRepository.findIdsByTaskNames(names)

    suspend fun getUnsavedTaskNames(): List<String> {
        val allTasksNames = tasksRepository.findAll().mapNotNull { it.taskName }
        val recordedTasksNames = tasksHistoryRepository.findAll().mapNotNull { it.name }
        return allTasksNames - recordedTasksNames
    }

    suspend fun getRequirementsSatisfiedById(id : Long) : Boolean =
        withContext(Dispatchers.IO){
        tasksHistoryService.RequirementsSatisfied(id)
    }

    suspend fun createTaskHistoriesByRequests(taskHistoryRequests : List<TasksHistoryRequest>) : List<TasksHistoryResponse> =
        withContext(Dispatchers.IO){
            tasksHistoryService.createTaskHistories(taskHistoryRequests)
    }

    fun recommendTasksByTrie(workInfo: String, unsavedTasks: List<String>): List<String> {
        return trieRecommender.recommendTasksByTrie(workInfo, unsavedTasks)
    }
}