package org.github.hrautoassignertaskhoursforecast.taskHistory.infrastructure.jdbc

import io.lettuce.core.dynamic.annotation.Param
import org.github.hrautoassignertaskhoursforecast.taskHistory.domain.entity.TasksHistory
import org.github.hrautoassignertaskhoursforecast.global.TaskState
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Repository
interface TasksHistoryJpaRepository: JpaRepository<TasksHistory, Long>, JpaSpecificationExecutor<TasksHistory> {
    fun findAllByRequirementsSatisfied(requirementsSatisfied: Boolean): List<TasksHistory>

    @Query(value = """ SELECT * FROM tasks_history
    WHERE available_jobs LIKE %:role%""",
        nativeQuery = true)
    fun findAllByEmployeeRolesLikeNative(@Param("role") role: String): List<TasksHistory>

    @Query(value = """SELECT * FROM tasks_history
    WHERE teammembers LIKE %:name%""",
        nativeQuery = true)
    fun findAllByTeamMembers(@Param("name") name: String): List<TasksHistory>

    @Transactional
    @Modifying
    @Query(value = """UPDATE tasks_history SET state = :state, spending_days = :spendingDays, ended_at = :endedAt 
        WHERE name = :historyName
    """, nativeQuery = true)
    fun updateStateAndEndedAt(
        @Param("historyName") historyName: String,
        @Param("state") state: String,
        @Param("spendingDays") spendingDays: Float?,
        @Param("endedAt") endedAt: LocalDate?
    ): Int

    @Query(value = """UPDATE tasks_history SET state = :state, spending_days = :spendingDays, 
        started_at = :startedAt, ended_at = :endedAt 
        WHERE name = :historyName
    """, nativeQuery = true)
    fun UpdateStateAndSpendingDaysAndStartedAtAndEndedAt(
        @Param("historyName") historyName: String,
        @Param("state") state: String,
        @Param("spendingDays") spendingDays: Float?,
        @Param("startedAt") startedAt: LocalDate?,
        @Param("endedAt") endedAt: LocalDate?
    ): Int

    fun findByName(name: String): TasksHistory
    fun findByTeamMembersContains(teamMemberName: String): List<TasksHistory>
    fun findAllByNameContainingIgnoreCase(name: String): List<TasksHistory>
    fun findAllByState(state: TaskState): List<TasksHistory>

    fun findAllByStartedAt(startedAt: LocalDate): List<TasksHistory>
    fun findAllByEndedAt(startedAt: LocalDate): List<TasksHistory>
    fun findAllBySpendingDays(spendingDays: Float): List<TasksHistory>
    fun findAllBySpendingDaysGreaterThanEqual(spendingDays: Float): List<TasksHistory>

}