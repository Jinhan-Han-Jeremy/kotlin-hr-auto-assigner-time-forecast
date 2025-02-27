package org.github.hrautoassignertaskhoursforecast.taskHistory.application.service

import kotlinx.coroutines.*
import org.github.hrautoassignertaskhoursforecast.analyzingCalculator.application.dto.AssignedTeamsForTasksDto
import org.github.hrautoassignertaskhoursforecast.global.RoleType.Companion.toDisplayNameList
import org.github.hrautoassignertaskhoursforecast.taskHistory.infrastructure.jdbc.TasksHistoryRepository
import org.github.hrautoassignertaskhoursforecast.taskHistory.domain.entity.TasksHistory
import org.springframework.stereotype.Service
import java.time.LocalDate

import org.github.hrautoassignertaskhoursforecast.taskHistory.application.TasksHistoryMapper
import org.github.hrautoassignertaskhoursforecast.taskHistory.application.dto.TasksHistoryRequest
import org.github.hrautoassignertaskhoursforecast.taskHistory.application.dto.TasksHistoryResponse
import org.github.hrautoassignertaskhoursforecast.global.TaskState
import org.github.hrautoassignertaskhoursforecast.global.exception.ResourceNotFoundException
import org.github.hrautoassignertaskhoursforecast.task.infrastructure.jdbc.TaskRepository
import org.github.hrautoassignertaskhoursforecast.teamMembers.application.service.TeamMemberRankingService
import org.github.hrautoassignertaskhoursforecast.teamMembers.application.service.TeamMemberService
import org.github.hrautoassignertaskhoursforecast.workStream.application.service.TaskProcessorFacade
import java.time.temporal.ChronoUnit

@Service
class TasksHistoryService(
    private val tasksHistoryRepository: TasksHistoryRepository,
    private val tasksHistoryMapper: TasksHistoryMapper,
    private val taskRepository: TaskRepository,
    private val teamMemberService: TeamMemberService,
    private val teamMemberRankingService: TeamMemberRankingService
) {

    suspend fun findAll(): List<TasksHistory> {
        return tasksHistoryRepository.findAll()  //비동기적으로 실행됨
    }

    //단순 TaskHistory 생성 및 db에 저장
    suspend fun createTaskHistory(requestDTO: TasksHistoryRequest): TasksHistoryResponse {
        val newTaskHistoryEntity = tasksHistoryMapper.toEntity(requestDTO)
        val savedTaskHistory = tasksHistoryRepository.save(newTaskHistoryEntity)
        return tasksHistoryMapper.toResponse(savedTaskHistory)
    }

    suspend fun updateRequirementsSatisfied(id: Long, satisfied: Boolean) {
            // tasksHistoryRepository`에서 ID로 엔티티 가져오기
            val taskHistory = tasksHistoryRepository.findById(id)

            if (taskHistory != null) {
                // satisfied 값을 비교 후 업데이트
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

    //facade 형태를 통하여 TaskHistory를 복수 생성 및 db에 저장 List<TasksHistoryResponse>데이터 타입 형태가 리스트
    suspend fun createTaskHistories(taskHistories: List<TasksHistoryRequest>): List<TasksHistoryResponse> {
            // 요청 DTO를 엔티티로 매핑
            val entities = taskHistories.map { taskHistoryRequest ->
                tasksHistoryMapper.toEntity(taskHistoryRequest)
            }
            // entities를 DB에 저장
            val savedEntities = tasksHistoryRepository.saveAll(entities)

            return savedEntities.map { tasksHistoryMapper.toResponse(it) }
        }

    private suspend fun updateTeamMembersState(existingHistory: TasksHistory) = coroutineScope {
        existingHistory.teamMembers?.membersNames?.let { memberNames ->
            val updateJobs = memberNames.map { memberName ->
                async {
                    val memberId = teamMemberService.findTeamMemberByName(memberName).id
                    if(existingHistory.state == TaskState.IN_PROGRESS) {
                        teamMemberService.updateTeamMemberState(memberId, false)
                    }
                    else{
                        teamMemberService.updateTeamMemberState(memberId, true)
                    }
                }
            }
            updateJobs.awaitAll() // 모든 업데이트 작업이 끝날 때까지 대기
        }
    }

    // Done 상태로 변경하는 함수
    suspend fun updateStateAndEndDateToDone(historyName: String, state: TaskState?, endedAt: LocalDate?): Boolean
            = coroutineScope {
        // 기존 TaskHistory 조회
        val existingHistory = tasksHistoryRepository.findByName(historyName) ?: return@coroutineScope false

        // state가 없으면 기존 상태 유지
        val newState = state ?: existingHistory.state
        // endedAt이 없으면 기존 값 유지
        val newEndedAt = endedAt ?: existingHistory.endedAt
        // 시작 시간 가져오기
        val startDateTimeInHistory = existingHistory.startedAt

        // spendingDays 계산
        val spendingDays = if (startDateTimeInHistory != null && newEndedAt != null) {
            ChronoUnit.HOURS.between(startDateTimeInHistory, newEndedAt) / 24.0f
        } else {
            0f
        }

        // 상태 및 종료 날짜 업데이트 (업데이트 성공 여부 확인)
        val isUpdated = tasksHistoryRepository.updateStateAndEndedAt(
            historyName, newState.value, spendingDays, newEndedAt
        ) > 0

        // 업데이트가 성공한 경우, 팀 멤버 상태 업데이트 함수 호출
        if (isUpdated) {
            updateTeamMembersState(existingHistory)
        }

        teamMemberRankingService.updateTeamMemberAchievementsScore(historyName)

        return@coroutineScope isUpdated
    }

    // InProgress 또는 NotStarted 상태로 업데이트
    suspend fun updateStateAndEndDateToInProgressOrNotStarted(historyName: String, state: TaskState?): Boolean {
        val existingHistory = tasksHistoryRepository.findByName(historyName) ?: return false

        // state가 없으면 기존 상태 유지
        val newState = state ?: existingHistory.state

        // 시작 날짜 설정
        val startDateTimeInHistory = if (existingHistory.state == TaskState.NOT_STARTED) {
            updateTeamMembersState(existingHistory)
            // 기존 상태가 NotStarted인 경우, 시작 시간을 null로 설정
            null
        } else {
            updateTeamMembersState(existingHistory)
            existingHistory.startedAt // 기존 값 유지
        }

        // 상태 및 시작 날짜 업데이트
        return tasksHistoryRepository.updateStateAndStartedAtAndEndedAt(
            historyName, newState.value, null, startDateTimeInHistory, null
        ) > 0
    }

    // 상태 업데이트를 수행하는 메인 함수
    suspend fun updateStateAndEndDate(historyName: String, state: TaskState?, endedAt: LocalDate?): Boolean {
        return when (state) {
            TaskState.DONE -> updateStateAndEndDateToDone(historyName, state, endedAt) // Done 상태 처리
            TaskState.IN_PROGRESS, TaskState.NOT_STARTED -> updateStateAndEndDateToInProgressOrNotStarted(historyName, state) // InProgress 또는 NotStarted 상태 처리
            else -> false // 그 외 상태는 처리하지 않음
        }
    }

    suspend fun deleteById(id: Long) {

            if (!tasksHistoryRepository.existsById(id)) {
            throw ResourceNotFoundException("TaskHistory with id $id not found")
        }
        return tasksHistoryRepository.deleteById(id)
    }

    suspend fun findById(id: Long): TasksHistory {
        return tasksHistoryRepository.findById(id)
    }

    suspend fun findAllByRequirementsSatisfied(requirementsSatisfied: Boolean): List<TasksHistory> {

        return tasksHistoryRepository.findAllByRequirementsSatisfied(requirementsSatisfied)
    }

    //requirementsSatisfied 체커 함수
    suspend fun requirementsSatisfied(id: Long): Boolean = coroutineScope {
        // 비동기 병렬 실행 (두 개의 DB 조회)
        val tasksHistoriesByDoneDeferred = async { findAllByState(TaskState.DONE) }
        val taskDeferred = async { taskRepository.findById(id) }

        // 결과 대기
        val tasksHistoriesByDone = tasksHistoriesByDoneDeferred.await()
        val task = taskDeferred.await()

        // task가 존재하지 않으면 false 반환
        if (task == null) {
            return@coroutineScope false
        }

        // 완료된 TaskHistory의 이름 리스트
        val doneNames = tasksHistoriesByDone.map { it.name }

        // requirementsList 확보
        val requirementsList = task.requirements?.requirementsList ?: emptyList()

        // requirementsList가 비어있지 않으며 모든 요소가 `doneNames`에 포함되는지 검사
        return@coroutineScope requirementsList.isNotEmpty() && requirementsList.all { it in doneNames }
    }

    suspend fun findAllByEmployeeRoles(role: String): List<TasksHistory> {

            return tasksHistoryRepository.findAllByEmployeeRolesLike(role)
    }

    suspend fun findAllByName(name: String): List<TasksHistory> {

        return  tasksHistoryRepository.findAllByNameContainingIgnoreCase(name)
    }

    suspend fun findAllByState(state: TaskState): List<TasksHistory> {

        return tasksHistoryRepository.findAllByState(state)
    }

    suspend fun findAllByTeamMemberName(name: String): List<TasksHistory> {

        return tasksHistoryRepository.findAllByTeamMembers(name)
    }

    suspend fun findAllByLessSpendingDays(spendingDays: Float): List<TasksHistory> =
        withContext(Dispatchers.IO) {

            tasksHistoryRepository.findAll().filter {
            val days = it.spendingDays ?: return@filter false
            days > 0 && spendingDays >= days
        }
    }

    suspend fun findAllByLongerThanSpendingDays(spendingDays: Float): List<TasksHistory> {

        return tasksHistoryRepository.findAllBySpendingDaysGreaterThanEqual(spendingDays)
    }

    suspend fun findAllBySpendingDays(spendingDays: Float): List<TasksHistory> {

        return tasksHistoryRepository.findAllBySpendingDays(spendingDays)
    }

    /**
     * 시작일부터 종료일까지(포함) 날짜를 1일씩 증가시키는 LocalDate 시퀀스 생성.
     */
    private fun generateDateSequence(start: LocalDate, end: LocalDate): Sequence<LocalDate> {
        return generateSequence(start) { date ->
            date.takeIf { it.isBefore(end.plusDays(1)) }?.plusDays(1)
        }
    }

    //async에선 @Transactional(readOnly = true)를 작용하지 않도록한다 이유는 충돌 방지
    suspend fun findByTimeInRange(startedDate: LocalDate, endedDate: LocalDate): List<TasksHistory> = coroutineScope {
        val dateList = generateDateSequence(startedDate, endedDate).toList() // Sequence → List 변환

        // 비동기 병렬 실행을 위해 async를 coroutineScope 내에서 실행
        //deferred를 꼭 적용하여 비동기를 완료
        val deferredResults: List<Deferred<List<TasksHistory>>> = dateList.map { date ->
            async {
                val startedAtTasks = tasksHistoryRepository.findAllByStartedAt(date) //  suspend 함수 호출
                val endedAtTasks = tasksHistoryRepository.findAllByEndedAt(date) //  suspend 함수 호출
                startedAtTasks + endedAtTasks //  두 리스트를 합침
            }
        }

        deferredResults.awaitAll().flatten().distinct() //  `awaitAll()` 실행 후 결과 처리
    }


    suspend fun createOrUpdateTasksHistory(
        assignedTeamsForTask: AssignedTeamsForTasksDto,
        rolesToUpdate: List<String>,
        startedAt: LocalDate?
    ): TasksHistoryResponse {
        val existingHistory = tasksHistoryRepository.findByName(assignedTeamsForTask.taskName)

        return if (existingHistory != null) {
            handleExistingTaskHistory(existingHistory, assignedTeamsForTask)
        } else {
            createNewTaskHistory(assignedTeamsForTask, rolesToUpdate, startedAt)
        }
    }

    //기존 TaskHistory가 존재하는 경우 업데이트 후 반환
    private suspend fun handleExistingTaskHistory(
        existingHistory: TasksHistory,
        assignedTeamsForTask: AssignedTeamsForTasksDto
    ): TasksHistoryResponse {
        val updatedState = TaskState.IN_PROGRESS
        updateStateAndEndDateToInProgressOrNotStarted(
            assignedTeamsForTask.taskName, updatedState
        )

        //상태가 업데이트된 기존 TaskHistory 객체를 변환하여 반환
        return mapToResponse(existingHistory.copy(state = updatedState))
    }

    //새로운 TaskHistory 생성 후 반환
    private suspend fun createNewTaskHistory(
        assignedTeamsForTask: AssignedTeamsForTasksDto,
        rolesToUpdate: List<String>,
        startedAt: LocalDate?
    ): TasksHistoryResponse {
        val request = TasksHistoryRequest(
            name = assignedTeamsForTask.taskName,
            teamMembers = assignedTeamsForTask.teamMembers,
            employeeRoles = rolesToUpdate,
            spendingDays = null,
            expectedDays = assignedTeamsForTask.duration.toFloat(),
            state = TaskState.IN_PROGRESS, // 초기 상태는 IN_PROGRESS
            requirementsSatisfied = true,
            startedAt = startedAt,
            endedAt = null
        )

        val newTaskHistory = createTaskHistory(request)
        return newTaskHistory
    }

    //TasksHistory -> TasksHistoryResponse 변환 (중복 제거)
    private fun mapToResponse(taskHistory: TasksHistory): TasksHistoryResponse {
        return TasksHistoryResponse(
            id = taskHistory.id,
            name = taskHistory.name,
            teamMembers = taskHistory.teamMembers?.toList(),  //teamMembers 변환 처리
            employeeRoles = taskHistory.employeeRoles.roles.toDisplayNameList(),
            spendingDays = taskHistory.spendingDays,
            expectedDays = taskHistory.expectedDays,
            state = taskHistory.state,
            requirementsSatisfied = taskHistory.requirementsSatisfied,
            startedAt = taskHistory.startedAt,
            endedAt = taskHistory.endedAt
        )
    }



}
