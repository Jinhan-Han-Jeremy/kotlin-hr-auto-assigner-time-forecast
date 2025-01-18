package org.github.hrautoassignertaskhoursforecast.WorkStream.application.service

import org.github.hrautoassignertaskhoursforecast.Task.infrastructure.jdbc.TaskRepository
import org.github.hrautoassignertaskhoursforecast.Task.domain.entity.Task
import org.github.hrautoassignertaskhoursforecast.TaskHistory.infrastructure.jdbc.TasksHistoryRepository
import org.github.hrautoassignertaskhoursforecast.utility.Trie.TrieRecommender
import org.springframework.stereotype.Service

@Service
class TaskProcessorFacade(
    private val tasksRepository: TaskRepository,
    private val tasksHistoryRepository: TasksHistoryRepository,
    private val trieRecommender: TrieRecommender // 새 의존성 추가
) {

    fun getTasksByIds(ids: List<Long>): List<Task>
    = tasksRepository.findAllById(ids)

    fun getNamesByIds(ids: List<Long>): List<String>
    = tasksRepository.findNamesByIds(ids)

    fun getIdsByTaskNames(names: List<String>): List<Long>
    = tasksRepository.findIdsByTaskNames(names)

    fun getUnsavedTaskNames(): List<String> {
        val allTasksNames = tasksRepository.findAll().mapNotNull { it.taskName }
        val recordedTasksNames = tasksHistoryRepository.findAll().mapNotNull { it.name }
        return allTasksNames - recordedTasksNames
    }

    fun recommendTasksByTrie(workInfo: String, unsavedTasks: List<String>): List<String> {
        return trieRecommender.recommendTasksByTrie(workInfo, unsavedTasks)
    }
}