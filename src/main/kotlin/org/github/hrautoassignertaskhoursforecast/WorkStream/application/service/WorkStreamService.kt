package org.github.hrautoassignertaskhoursforecast.WorkStream.application.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.github.hrautoassignertaskhoursforecast.Task.domain.entity.Task
import org.github.hrautoassignertaskhoursforecast.TaskHistory.application.TasksHistoryMapper
import org.github.hrautoassignertaskhoursforecast.TaskHistory.application.dto.TasksHistoryRequest
import org.github.hrautoassignertaskhoursforecast.TaskHistory.application.service.TasksHistoryService
import org.github.hrautoassignertaskhoursforecast.TaskHistory.domain.entity.TasksHistory
import org.github.hrautoassignertaskhoursforecast.WorkStream.application.WorkStreamMapper
import org.github.hrautoassignertaskhoursforecast.WorkStream.application.dto.AnalyzedWorkStreamResponse
import org.github.hrautoassignertaskhoursforecast.WorkStream.application.dto.TaskPairDto
import org.github.hrautoassignertaskhoursforecast.WorkStream.application.dto.WorkStreamRequest
import org.github.hrautoassignertaskhoursforecast.WorkStream.application.dto.WorkStreamResponse
import org.github.hrautoassignertaskhoursforecast.WorkStream.infrastructure.jdbc.WorkStreamRepository
import org.github.hrautoassignertaskhoursforecast.global.FastApiService
import org.github.hrautoassignertaskhoursforecast.global.TaskState
import org.github.hrautoassignertaskhoursforecast.global.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class WorkStreamService(
    private val workStreamRepository: WorkStreamRepository,
    private val workStreamMapper: WorkStreamMapper,
    private val fastApiService: FastApiService,
    private val taskProcessorFacade: TaskProcessorFacade,
    private val taskHistoryService: TasksHistoryService
) {

    /** DB에서 Task 목록을 불러온 뒤 null인 TaskName은 제외한 리스트 반환
     */
    private fun fetchTaskNamesFromDb(): List<String> {


        val unsavedTasksNamesInWorkStream = taskProcessorFacade.getUnsavedTaskNames()
        if (unsavedTasksNamesInWorkStream.isEmpty()) {
            throw ResourceNotFoundException("Task 목록이 비어 있습니다. 데이터를 확인해주세요.")
        }
        return unsavedTasksNamesInWorkStream
    }

    /** FastAPI를 호출하여 분석된 추천 Task 목록 받기
     */
    private suspend fun recommendTasksByFastApi(workInfo: String): List<String> {
        val taskNames = fastApiService.getTaskNamesFromAnalyzedTexts(workInfo)
        println("Text 분석 추천 작업: $taskNames")
        return taskNames
    }

//    suspend fun taskPairsAssignInTasksHistory(workInfo: String, ids: List<Long>):  WorkStreamResponse =
//        withContext(Dispatchers.IO) {
//            val tasks = taskProcessorFacade.getTasksByIds(ids)
//
//            val taskPairs = tasks.map { TaskPairDto(id = it.id, name = it.taskName) }
//
//            tasks.map{it.employeeRoles.roles.map { it.displayName}}
//
//            val taskHistoryRequests: List<TasksHistoryRequest> =
//
//            val name: String,
//            val teamMembers: List<String>? = null, // 팀원 이름 리스트
//            val employeeRoles: List<String> = emptyList(), // 직무 리스트
//            val spendingDays: Float? = null,
//            val expectedDays: Float? = null,
//            val state: TaskState,
//            val requirementsSatisfied: Boolean? = null,
//            val startedAt: LocalDate? = null,
//            val endedAt: LocalDate? = null
//
//             val teamMembers: List<String>? = null, // 팀원 이름 리스트
//             val spendingDays: Float? = null,
//             val expectedDays: Float? = null,
//             val state: TaskState,
//             val requirementsSatisfied: Boolean? = null,
//             val startedAt: LocalDate? = null,
//             val endedAt: LocalDate? = null
//
//            val distinctEmployeeRoles = tasks.flatMap { it.employeeRoles.roles }.distinct()
//
//            val taskHistoryRequest = TasksHistoryRequest(
//                name = taskPairs.first().name,
//                teamMembers, employeeRoles = distinctEmployeeRoles, spendingDays, expectedDays, state
//            )
//
//             val requestDTO = TasksHistoryMapper.toRequestDto(taskHistoryRequests)
//            val newTaskEntity = TasksHistoryMapper.toEntity(requestDTO)
//            taskHistoryService.createTaskHistory(newTaskEntity)
//
//             val workStreamRequest = WorkStreamRequest(
//                workInfo,
//                distinctEmployeeRoles.toString()
//            )
//
//             createWorkStream(workStreamRequest)
//        }

    suspend fun RequirementsSatisfied(task :Task): Boolean =
        withContext(Dispatchers.IO) {

            // done상태의 taskHistory 이름 리스트
            val tasksHistoryByDone = taskHistoryService.findAllByState(TaskState.DONE)

            val requirementsList = task.requirements?.requirementsList ?: emptyList()

            when {
                requirementsList.all { it in tasksHistoryByDone.map{ it.name}} -> true
                else -> false
            }
        }

    suspend fun createWorkStream(requestDTO: WorkStreamRequest):  WorkStreamResponse =
        withContext(Dispatchers.IO) {

            val newTaskEntity = workStreamMapper.toEntity(requestDTO)
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

    fun getWorkStreamById(id: Int): WorkStreamResponse {
        val workStream = workStreamRepository.findById(id)
            .orElseThrow { IllegalArgumentException("WorkStream with ID $id not found") }
        return workStreamMapper.toResponseDto(workStream)
    }

    fun getAllWorkStreams(): List<WorkStreamResponse> {
        return workStreamRepository.findAll().map { workStreamMapper.toResponseDto(it) }
    }
}