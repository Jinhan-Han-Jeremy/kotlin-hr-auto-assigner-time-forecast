package org.github.hrautoassignertaskhoursforecast.workStream.application.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.github.hrautoassignertaskhoursforecast.global.TaskState
import org.github.hrautoassignertaskhoursforecast.task.infrastructure.jdbc.TaskRepository
import org.github.hrautoassignertaskhoursforecast.task.domain.entity.Task
import org.github.hrautoassignertaskhoursforecast.taskHistory.application.dto.TasksHistoryRequest
import org.github.hrautoassignertaskhoursforecast.taskHistory.application.dto.TasksHistoryResponse
import org.github.hrautoassignertaskhoursforecast.taskHistory.application.service.TasksHistoryService
import org.github.hrautoassignertaskhoursforecast.taskHistory.infrastructure.jdbc.TasksHistoryRepository
import org.github.hrautoassignertaskhoursforecast.utility.Trie.TrieRecommender
import org.springframework.stereotype.Service
import org.springframework.web.servlet.function.ServerResponse.async

@Service
class TaskProcessorFacade(
    private val tasksRepository: TaskRepository,
    private val tasksHistoryRepository: TasksHistoryRepository,
    private val tasksHistoryService: TasksHistoryService,
    private val trieRecommender: TrieRecommender // 새 의존성 추가
) {

    suspend fun getTasksByIds(ids: List<Long>): List<Task>
    = tasksRepository.findAllById(ids)

    suspend fun getTaskById(id: Long): Task
    = tasksRepository.findById(id)

    suspend fun getNamesByIds(ids: List<Long>): List<String>
    = tasksRepository.findNamesByIds(ids)

    suspend fun getIdsByTaskNames(names: List<String>): List<Long>
    = tasksRepository.findIdsByTaskNames(names)

    //
    suspend fun getUnsavedTaskNames(): List<String> = coroutineScope {
        val allTasksDeferred = async { tasksRepository.findAll().mapNotNull { it.taskName } }
        val recordedTasksDeferred = async { tasksHistoryRepository.findAll().mapNotNull { it.name } }

        val allTasksNames = allTasksDeferred.await()
        val recordedTasksNames = recordedTasksDeferred.await()

        allTasksNames - recordedTasksNames //return 문 없이 바로 결과 반환 가능
    }

    suspend fun getRequirementsSatisfiedById(id: Long): Boolean {
        return tasksHistoryService.requirementsSatisfied(id)
    }

    suspend fun getRequirementsSatisfiedByChanges(id: Long): Boolean = coroutineScope {
        val taskDeferred = async { tasksRepository.findById(id) }

        val tasksHistoryDoneDeferred = async { tasksHistoryService.findAllByState(TaskState.DONE) }

        val task = taskDeferred.await()
        val tasksHistoryDoneNames = tasksHistoryDoneDeferred.await().map { it.name }

        val requirementsList = task.requirements?.requirementsList ?: emptyList()

        print("requirementsLDone " + tasksHistoryDoneNames)
        print("requirementsList " + requirementsList.toSet())

        tasksHistoryDoneNames.toSet().containsAll(requirementsList.toSet())
    }

    suspend fun createTaskHistoriesByRequests(taskHistoryRequests : List<TasksHistoryRequest>) : List<TasksHistoryResponse>
    = tasksHistoryService.createTaskHistories(taskHistoryRequests)

    fun recommendTasksByTrie(workInfo: String, unsavedTasks: List<String>): List<String> {
        return trieRecommender.recommendTasksByTrie(workInfo, unsavedTasks)
    }

}