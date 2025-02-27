package org.github.hrautoassignertaskhoursforecast.workStream.application.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import org.github.hrautoassignertaskhoursforecast.taskHistory.application.dto.TasksHistoryRequest
import org.github.hrautoassignertaskhoursforecast.workStream.application.WorkStreamMapper
import org.github.hrautoassignertaskhoursforecast.workStream.infrastructure.jdbc.WorkStreamRepository
import org.github.hrautoassignertaskhoursforecast.global.FastApiService
import org.github.hrautoassignertaskhoursforecast.global.RoleType
import org.github.hrautoassignertaskhoursforecast.global.TaskState
import org.github.hrautoassignertaskhoursforecast.global.exception.ResourceNotFoundException
import org.github.hrautoassignertaskhoursforecast.task.domain.entity.Task
import org.github.hrautoassignertaskhoursforecast.workStream.application.dto.*
import org.springframework.stereotype.Service

@Service
class WorkStreamService(
    private val workStreamRepository: WorkStreamRepository,
    private val workStreamMapper: WorkStreamMapper,
    private val fastApiService: FastApiService,
    private val taskProcessorFacade: TaskProcessorFacade
) {

    /** DB에서 Task 목록을 불러온 뒤 null인 TaskName은 제외한 리스트 반환
     */
    suspend private fun fetchTaskNamesFromDb(): List<String> {

        val unsavedTasksNamesInWorkStream = taskProcessorFacade.getUnsavedTaskNames()
        if (unsavedTasksNamesInWorkStream.isEmpty()) {
            throw ResourceNotFoundException("Task 목록이 비어 있습니다. 데이터를 확인해주세요.")
        }
        return unsavedTasksNamesInWorkStream
    }

    suspend fun getAllWorkStreams(): List<WorkStreamResponse> =
        withContext(Dispatchers.IO) {
            workStreamRepository.findAll().map { workStreamMapper.toResponseDto(it) }
        }

    // db에 분석된 새로운 task들 할당
    suspend fun tasksAndWorkInfoAssignInTasksHistory(workInfo: String, ids: List<Long>): WorkStreamResponseWithTasks {
        // 순차적으로 Tasks 조회
        val tasks = taskProcessorFacade.getTasksByIds(ids)
        val tasksNames: List<String> = tasks.map { it.taskName } // 미래에 taskName 리스트

        println("tasksAndWorkInfoAssignInTasksHistory tasks: $tasks")

        // TaskHistoryRequest 리스트 생성 (분리된 함수 사용)
        val taskHistoryRequests = createTaskHistoryRequests(tasks)

        // TaskHistory 할당 (순차 실행)
        taskProcessorFacade.createTaskHistoriesByRequests(taskHistoryRequests)

        val distinctEmployeeRoles = tasks.flatMap { it.employeeRoles.roles }.distinct()

        //WorkStream 생성 (순차 실행)
        return createWorkStream(workInfo, distinctEmployeeRoles, tasksNames)
    }

    //분리된 TaskHistoryRequest 생성 함수
    private suspend fun createTaskHistoryRequests(tasks: List<Task>): List<TasksHistoryRequest> {
        // 순차 실행: `getRequirementsSatisfiedByChanges(task.id)` 실행 후 결과 저장
        val requirementsResults = tasks.map { task ->
            taskProcessorFacade.getRequirementsSatisfiedByChanges(task.id)
        }

        return tasks.mapIndexed { index, task ->
            TasksHistoryRequest(
                name = task.taskName,
                teamMembers = null,
                employeeRoles = task.employeeRoles.roles.map { it.displayName },
                spendingDays = null,
                expectedDays = null,
                state = TaskState.NOT_STARTED,
                requirementsSatisfied = requirementsResults[index], //순차 실행 후 값 적용
                startedAt = null,
                endedAt = null
            )
        }
    }


    suspend fun createWorkStream(workInfo: String, distinctEmployeeRoles: List<RoleType>, tasksNames: List<String>): WorkStreamResponseWithTasks {
        val workStreamRequest = WorkStreamRequest(
            workInfo,
            distinctEmployeeRoles.toString()
        )

        val newTaskEntity = workStreamMapper.toEntity(workStreamRequest)
        val savedWorkedStream = workStreamRepository.save(newTaskEntity)

        return workStreamMapper.toResponseWithTasksDto(savedWorkedStream, tasksNames)
    }

    suspend fun getTaskPairsByNames(names: List<String>):  List<TaskPairDto> =
        withContext(Dispatchers.IO) {

        val ids = taskProcessorFacade.getIdsByTaskNames(names)

        // 이름으로 ID를 찾은 결과와 요청한 이름의 차집합 계산
        val foundNames = taskProcessorFacade.getNamesByIds(ids)
        val missingNames = names - foundNames

        // 예외 처리
        if (missingNames.isNotEmpty()) {
            throw ResourceNotFoundException("Tasks not found for names: ${missingNames.joinToString(", ")}")
        }

        // ID와 이름을 매핑하여 TaskDto 리스트 생성
        ids.zip(foundNames).map { (id, name) -> TaskPairDto(id = id, name = name) }
    }

    //Fastapi cosine similarity와 TfidfVectorizer를 활용하여 분석
    private suspend fun getIdsNamesPairsByFastApi(workInfo: String):  List<TaskPairDto> {
        // FastAPI를 통한 작업 추출
        val recommendedNamesByFastApi = fastApiService.recommendTasksByFastApi(workInfo)
        val idsNamesPairsByFastApi = getTaskPairsByNames(recommendedNamesByFastApi)
        return idsNamesPairsByFastApi
    }

    /** 최종으로 세 함수들을 연달아 호출하여 결과를 합치고,
     * WorkStreamResponse 형태로 매핑하여 반환
     */
    suspend fun analyzedWorkStream(workInfo: String, autoAssignAndAnalysis: Boolean): AnalyzedWorkStreamResponse {

        var idsNamesPairsByTrie: List<TaskPairDto> = emptyList() // var로 변경
        if (!autoAssignAndAnalysis) {
            // DB에서 TaskName 목록 가져오기
            val unsavedTasksNamesInWorkStream = fetchTaskNamesFromDb()

            // Trie 기반 상위 작업 추출
            val recommendedNamesByTrie = taskProcessorFacade.recommendTasksByTrie(workInfo, unsavedTasksNamesInWorkStream)
            idsNamesPairsByTrie = getTaskPairsByNames(recommendedNamesByTrie) // 정상적으로 할당 가능
        }

        // FastAPI를 통한 작업 추출
        val idsNamesPairsByFastApi: List<TaskPairDto> = getIdsNamesPairsByFastApi(workInfo)

        // 두 결과 병합
        val mergedTasks = (idsNamesPairsByTrie + idsNamesPairsByFastApi).distinctBy { it.id }
        println("최종 추천 작업: $mergedTasks")

        // WorkStreamResponse 매핑 및 반환
        return workStreamMapper.toResponseAnalysisDto(
            workstream = workInfo,
            tasksByTrieAnalysis = idsNamesPairsByTrie,
            tasksByTextAnalysis = idsNamesPairsByFastApi,
            analyzedTasks = mergedTasks
        )

    }

    private fun extractPrefix(workInfo: String): String {
        // 공백으로 분리한 첫 5단어를 접두사로 사용
        return workInfo.split(" ").take(5).joinToString(" ")
    }

    suspend fun getWorkStreamById(id: Int): WorkStreamResponse =
        withContext(Dispatchers.IO) {
        val workStream = workStreamRepository.findById(id)
            .orElseThrow { IllegalArgumentException("WorkStream with ID $id not found") }

            workStreamMapper.toResponseDto(workStream)
    }
}