package org.github.hrautoassignertaskhoursforecast.TaskHistory.infrastructure.jdbc

import io.lettuce.core.dynamic.annotation.Param
import org.github.hrautoassignertaskhoursforecast.TaskHistory.domain.entity.TasksHistory
import org.github.hrautoassignertaskhoursforecast.global.TaskState
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface TasksHistoryRepository: JpaRepository<TasksHistory, Long>, JpaSpecificationExecutor<TasksHistory> {
    fun findAllByRequirementsSatisfied(requirementsSatisfied: Boolean): List<TasksHistory>

    @Query(value = """ SELECT * FROM tasks_history
    WHERE available_jobs LIKE %:role%""",
        nativeQuery = true)
    fun findAllByEmployeeRolesLikeNative(@Param("role") role: String): List<TasksHistory>

    @Query(value = """SELECT * FROM tasks_history
    WHERE teammembers LIKE %:name%""",
        nativeQuery = true)
    fun findAllByTeamMembers(@Param("name") name: String): List<TasksHistory>

    fun findByName(name: String): TasksHistory
    fun findByTeamMembersContains(teamMemberName: String): List<TasksHistory>
    fun findAllByNameContainingIgnoreCase(name: String): List<TasksHistory>
    fun findAllByState(state: TaskState): List<TasksHistory>

    fun findAllByStartedAt(startedAt: LocalDate): List<TasksHistory>
    fun findAllByEndedAt(startedAt: LocalDate): List<TasksHistory>
    fun findAllBySpendingDays(spendingDays: Float): List<TasksHistory>
    fun findAllBySpendingDaysGreaterThanEqual(spendingDays: Float): List<TasksHistory>

}