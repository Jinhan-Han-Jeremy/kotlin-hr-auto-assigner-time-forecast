package org.github.hrautoassignertaskhoursforecast.taskHistory.infrastructure.jdbc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.github.hrautoassignertaskhoursforecast.global.TaskState
import org.github.hrautoassignertaskhoursforecast.global.exception.ResourceNotFoundException
import org.github.hrautoassignertaskhoursforecast.taskHistory.domain.entity.TasksHistory
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class TasksHistoryRepository(private val tasksHistoryJpaRepository: TasksHistoryJpaRepository) {

    suspend fun findById(id: Long): TasksHistory = withContext(Dispatchers.IO) {
        tasksHistoryJpaRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Task history with ID $id not found")
        }
    }

    suspend fun findAll(): List<TasksHistory> = withContext(Dispatchers.IO) {
        tasksHistoryJpaRepository.findAll()
    }

    suspend fun findAllByRequirementsSatisfied(requirementsSatisfied: Boolean): List<TasksHistory> = withContext(Dispatchers.IO) {
        tasksHistoryJpaRepository.findAllByRequirementsSatisfied(requirementsSatisfied)
    }

    suspend fun findAllByEmployeeRolesLike(role: String): List<TasksHistory> = withContext(Dispatchers.IO) {
        tasksHistoryJpaRepository.findAllByEmployeeRolesLikeNative(role)
    }

    suspend fun findAllByTeamMembers(name: String): List<TasksHistory> = withContext(Dispatchers.IO) {
        tasksHistoryJpaRepository.findAllByTeamMembers(name)
    }

    suspend fun findByName(name: String): TasksHistory = withContext(Dispatchers.IO) {
        tasksHistoryJpaRepository.findByName(name)
    }
    suspend fun findByTeamMembersContains(teamMemberName: String): List<TasksHistory> = withContext(Dispatchers.IO) {
        tasksHistoryJpaRepository.findByTeamMembersContains(teamMemberName)
    }

    suspend fun findAllByNameContainingIgnoreCase(name: String): List<TasksHistory> = withContext(Dispatchers.IO) {
        tasksHistoryJpaRepository.findAllByNameContainingIgnoreCase(name)
    }

    suspend fun findAllByState(state: TaskState): List<TasksHistory> = withContext(Dispatchers.IO) {
        tasksHistoryJpaRepository.findAllByState(state)
    }

    suspend fun findAllByStartedAt(startedAt: LocalDate): List<TasksHistory> = withContext(Dispatchers.IO) {
        tasksHistoryJpaRepository.findAllByStartedAt(startedAt)
    }

    suspend fun findAllByEndedAt(endedAt: LocalDate): List<TasksHistory> = withContext(Dispatchers.IO) {
        tasksHistoryJpaRepository.findAllByEndedAt(endedAt)
    }

    suspend fun findAllBySpendingDays(spendingDays: Float): List<TasksHistory> = withContext(Dispatchers.IO) {
        tasksHistoryJpaRepository.findAllBySpendingDays(spendingDays)
    }

    suspend fun findAllBySpendingDaysGreaterThanEqual(spendingDays: Float): List<TasksHistory> = withContext(Dispatchers.IO) {
        tasksHistoryJpaRepository.findAllBySpendingDaysGreaterThanEqual(spendingDays)
    }
    suspend fun existsById(id : Long)= withContext(Dispatchers.IO) {
        tasksHistoryJpaRepository.existsById(id)
    }

    suspend fun updateStateAndEndedAt(historyName:String, newState:String, spendingDays: Float?, newEndedAt: LocalDate?) = withContext(Dispatchers.IO) {
        tasksHistoryJpaRepository.updateStateAndEndedAt(historyName, newState, spendingDays, newEndedAt)
    }

    suspend fun updateStateAndStartedAtAndEndedAt(historyName:String, newState:String, spendingDays: Float?, newStartedAt: LocalDate?, newEndedAt: LocalDate?) = withContext(Dispatchers.IO) {
        tasksHistoryJpaRepository.UpdateStateAndSpendingDaysAndStartedAtAndEndedAt(historyName, newState, spendingDays, newStartedAt, newEndedAt)
    }

    suspend fun save(taskHistory: TasksHistory): TasksHistory = withContext(Dispatchers.IO) {
        tasksHistoryJpaRepository.save(taskHistory)
    }

    suspend fun saveAll(taskHistory: List<TasksHistory>): List<TasksHistory> = withContext(Dispatchers.IO) {
        tasksHistoryJpaRepository.saveAll(taskHistory)
    }

    suspend fun delete(taskHistory: TasksHistory) = withContext(Dispatchers.IO) {
        tasksHistoryJpaRepository.delete(taskHistory)
    }

    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        tasksHistoryJpaRepository.deleteById(id)
    }
}