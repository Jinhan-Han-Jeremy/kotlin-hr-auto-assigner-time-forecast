package org.github.hrautoassignertaskhoursforecast.TaskHistory.application.service

import org.github.hrautoassignertaskhoursforecast.TaskHistory.infrastructure.jdbc.TasksHistoryRepository
import org.github.hrautoassignertaskhoursforecast.TaskHistory.domain.entity.TasksHistory
import org.springframework.stereotype.Service
import java.time.LocalDate


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.github.hrautoassignertaskhoursforecast.global.TaskState
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class TasksHistoryService(
    private val tasksHistoryRepository: TasksHistoryRepository
) {

    @Transactional(readOnly = true)
    suspend fun findAll(): List<TasksHistory> =
        withContext(Dispatchers.IO) {

            tasksHistoryRepository.findAll()
    }

    suspend fun createTaskHistory(taskHistory: TasksHistory): TasksHistory =
        withContext(Dispatchers.IO) {

            tasksHistoryRepository.save(taskHistory)
    }

    suspend fun deleteById(id: Long) =
        withContext(Dispatchers.IO) {

            if (!tasksHistoryRepository.existsById(id)) {
            throw NoSuchElementException("TaskHistory with id $id not found")
        }
        tasksHistoryRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    suspend fun findById(id: Long): TasksHistory =
        withContext(Dispatchers.IO) {

            tasksHistoryRepository.findById(id).orElseThrow {
            NoSuchElementException("TasksHistory with id $id not found")
        }
    }

    @Transactional(readOnly = true)
    suspend fun findAllByRequirementsSatisfied(requirementsSatisfied: Boolean): List<TasksHistory> =
        withContext(Dispatchers.IO) {

            tasksHistoryRepository.findAllByRequirementsSatisfied(requirementsSatisfied)
        }

    suspend fun updateRequirementsSatisfied(id: Long, satisfied: Boolean) =
        withContext(Dispatchers.IO) {
            // `tasksHistoryRepository`에서 ID로 엔티티 가져오기
            val taskHistory = tasksHistoryRepository.findById(id).orElse(null)

            if (taskHistory != null) {
                // `when` 조건으로 satisfied 값을 비교 후 업데이트
                when (taskHistory.requirementsSatisfied != satisfied) {
                    true -> {
                        // 조건이 만족하면 업데이트 수행
                        taskHistory.requirementsSatisfied = satisfied
                        tasksHistoryRepository.save(taskHistory) // 변경 사항 저장
                        println("TaskHistory ID $id: requirementsSatisfied 값을 $satisfied 로 업데이트했습니다.")
                    }
                    false -> {
                        // 변경 사항이 없음을 출력 (옵션)
                        println("TaskHistory ID $id: 값이 동일하므로 업데이트를 건너뜁니다.")
                    }
                }
            } else {
                // 엔티티를 찾지 못한 경우 처리
                println("TaskHistory ID $id: 찾을 수 없습니다.")
            }
        }

    @Transactional(readOnly = true)
    suspend fun findAllByEmployeeRoles(role: String): List<TasksHistory> =
        withContext(Dispatchers.IO) {

            tasksHistoryRepository.findAllByEmployeeRolesLikeNative(role)
        }

    @Transactional(readOnly = true)
    suspend fun findAllByName(name: String): List<TasksHistory> =
        withContext(Dispatchers.IO) {

            tasksHistoryRepository.findAllByNameContainingIgnoreCase(name)
    }

    @Transactional(readOnly = true)
    suspend fun findAllByState(state: TaskState): List<TasksHistory> =
        withContext(Dispatchers.IO) {

            tasksHistoryRepository.findAllByState(state)
    }

    @Transactional(readOnly = true)
    suspend fun findAllByTeamMemberName(name: String): List<TasksHistory> =
        withContext(Dispatchers.IO) {

            tasksHistoryRepository.findAllByTeamMembers(name)
    }

    @Transactional(readOnly = true)
    suspend fun findAllByLessSpendingDays(spendingDays: Float): List<TasksHistory> =
        withContext(Dispatchers.IO) {

            tasksHistoryRepository.findAll().filter {
            val days = it.spendingDays ?: return@filter false
            days > 0 && spendingDays >= days
        }
    }

    @Transactional(readOnly = true)
    suspend fun findAllByLongerThanSpendingDays(spendingDays: Float): List<TasksHistory> =
        withContext(Dispatchers.IO) {

            tasksHistoryRepository.findAllBySpendingDaysGreaterThanEqual(spendingDays)
        }

    @Transactional(readOnly = true)
    suspend fun findAllBySpendingDays(spendingDays: Float): List<TasksHistory> =
        withContext(Dispatchers.IO) {

            tasksHistoryRepository.findAllBySpendingDays(spendingDays)
    }

    @Transactional(readOnly = true)
    suspend fun findByTimeInRange(startedDate: LocalDate, endedDate: LocalDate): List<TasksHistory>
    = withContext(Dispatchers.IO) {
            // 날짜 시퀀스 생성
            val dateSequence = generateDateSequence(startedDate, endedDate)

            // 각 날짜에 대해 startedAt과 endedAt 조건으로 레코드 조회
            dateSequence.flatMap { date ->
                val startedAtTasks = tasksHistoryRepository.findAllByStartedAt(date)
                val endedAtTasks = tasksHistoryRepository.findAllByEndedAt(date)
                startedAtTasks + endedAtTasks // 두 리스트를 합침
            }
                .distinct() // 중복된 레코드 제거
                .toList() // Sequence를 List로 변환
    }

    /**
     * 시작일부터 종료일까지(포함) 날짜를 1일씩 증가시키는 LocalDate 시퀀스 생성.
     */
    private fun generateDateSequence(start: LocalDate, end: LocalDate): Sequence<LocalDate> {
        return generateSequence(start) { date ->
            date.takeIf { it.isBefore(end) || it.isEqual(end) }?.plusDays(1)
        }
    }
}
