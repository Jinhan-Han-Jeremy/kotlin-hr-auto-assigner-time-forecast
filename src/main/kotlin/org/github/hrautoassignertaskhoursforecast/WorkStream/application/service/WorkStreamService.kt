package org.github.hrautoassignertaskhoursforecast.WorkStream.application.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.github.hrautoassignertaskhoursforecast.Task.application.dto.TaskResponseDTO

import org.github.hrautoassignertaskhoursforecast.TaskHistory.application.dto.TasksHistoryRequest
import org.github.hrautoassignertaskhoursforecast.TaskHistory.application.service.TasksHistoryService
import org.github.hrautoassignertaskhoursforecast.WorkStream.application.WorkStreamMapper
import org.github.hrautoassignertaskhoursforecast.WorkStream.application.dto.AnalyzedWorkStreamResponse
import org.github.hrautoassignertaskhoursforecast.WorkStream.application.dto.TaskPairDto
import org.github.hrautoassignertaskhoursforecast.WorkStream.application.dto.WorkStreamRequest
import org.github.hrautoassignertaskhoursforecast.WorkStream.application.dto.WorkStreamResponse
import org.github.hrautoassignertaskhoursforecast.WorkStream.infrastructure.jdbc.WorkStreamRepository
import org.github.hrautoassignertaskhoursforecast.global.FastApiService
import org.github.hrautoassignertaskhoursforecast.global.RoleType
import org.github.hrautoassignertaskhoursforecast.global.TaskState
import org.github.hrautoassignertaskhoursforecast.global.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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

    @Transactional(readOnly = true)
    suspend fun findAllWorkStream(): List<WorkStreamResponse> =
        withContext(Dispatchers.IO) {

            workStreamRepository.findAll().map { workStreamMapper.toResponseDto(it) }
        }

    /** FastAPI를 호출하여 분석된 추천 Task 목록 받기
     */
    private suspend fun recommendTasksByFastApi(workInfo: String): List<String> {
        val taskNames = fastApiService.getTaskNamesFromAnalyzedTexts(workInfo)
        println("Text 분석 추천 작업: $taskNames")
        return taskNames
    }

    // db에 분석된 새로운 task들 할당
    suspend fun tasksAndWorkInfoAssignInTasksHistory(workInfo: String, ids: List<Long>):  WorkStreamResponse =
        withContext(Dispatchers.IO) {
            // Tasks 조회
            val tasks = taskProcessorFacade.getTasksByIds(ids)

            print("tasksAndWorkInfoAssignInTasksHistory tasks:$tasks")

            val taskHistoryRequests: List<TasksHistoryRequest> =
                tasks.map { task ->
                    TasksHistoryRequest(
                        name = task.taskName,
                        teamMembers = null,
                        employeeRoles = task.employeeRoles.roles.map { it.displayName },
                        spendingDays = null,
                        expectedDays = null,
                        state = TaskState.NOT_STARTED,
                        requirementsSatisfied = taskProcessorFacade.getRequirementsSatisfiedById(task.id),
                        startedAt = null,
                        endedAt = null
                        )
                }

            val distinctEmployeeRoles = tasks.flatMap { it.employeeRoles.roles }.distinct()

            //TaskHistory 할당
            taskProcessorFacade.createTaskHistoriesByRequests(taskHistoryRequests)

            // WorkStream 생성
            createWorkStream(workInfo, distinctEmployeeRoles)

        }

    suspend fun createWorkStream(workInfo: String, distinctEmployeeRoles: List<RoleType>):  WorkStreamResponse =
        withContext(Dispatchers.IO) {

            val workStreamRequest = WorkStreamRequest(
                workInfo,
                distinctEmployeeRoles.toString()
            )

            val newTaskEntity = workStreamMapper.toEntity(workStreamRequest)
            val savedWorkedStream = workStreamRepository.save(newTaskEntity)

            workStreamMapper.toResponseDto(savedWorkedStream)
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
        ids.zip(names).map { (id, name) -> TaskPairDto(id = id, name = name) }
    }

    /** 최종으로 세 함수들을 연달아 호출하여 결과를 합치고,
     * WorkStreamResponse 형태로 매핑하여 반환
     */
    suspend fun analyzedWorkStream(workInfo: String): AnalyzedWorkStreamResponse {
        // DB에서 TaskName 목록 가져오기
        val unsavedTasksNamesInWorkStream = fetchTaskNamesFromDb()

        // Trie 기반 상위 작업 추출
        val recommendedNamesByTrie = taskProcessorFacade.recommendTasksByTrie(workInfo, unsavedTasksNamesInWorkStream)
        val idsNamesPairsByTrie = getTaskPairsByNames(recommendedNamesByTrie)

        // FastAPI를 통한 작업 추출
        val recommendedByFastApi = recommendTasksByFastApi(workInfo)
        val idsNamesPairsByFastApi = getTaskPairsByNames(recommendedByFastApi)

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

    suspend fun getAllWorkStreams(): List<WorkStreamResponse> =
        withContext(Dispatchers.IO) {
            workStreamRepository.findAll().map { workStreamMapper.toResponseDto(it) }
    }
}