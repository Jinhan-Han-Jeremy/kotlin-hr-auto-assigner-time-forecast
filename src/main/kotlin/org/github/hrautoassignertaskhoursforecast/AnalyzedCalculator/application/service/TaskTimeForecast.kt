package org.github.hrautoassignertaskhoursforecast.AnalyzedCalculator.application.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.github.hrautoassignertaskhoursforecast.Task.application.TaskMapper
import org.github.hrautoassignertaskhoursforecast.Task.application.dto.TaskResponseDTO
import org.github.hrautoassignertaskhoursforecast.Task.domain.entity.Task
import org.github.hrautoassignertaskhoursforecast.Task.infrastructure.jdbc.TaskRepository
import org.github.hrautoassignertaskhoursforecast.TaskHistory.application.dto.TasksHistoryRequest
import org.github.hrautoassignertaskhoursforecast.TeamMembers.application.dto.TeamMemberResponseDTO
import org.github.hrautoassignertaskhoursforecast.TeamMembers.application.service.FilteredMembers
import org.github.hrautoassignertaskhoursforecast.global.exception.ResourceNotFoundException

class TaskTimeForecast (
    private val filteredMembers: FilteredMembers,
    private val taskRepository: TaskRepository,
    private val taskMapper: TaskMapper
){

    private suspend fun getSelectedTask(selectedTaskName: String): TaskResponseDTO =
        withContext(Dispatchers.IO) {
            val task = taskRepository.findByTaskName(selectedTaskName)
                ?: throw ResourceNotFoundException("Task with name '$selectedTaskName' not found")
            taskMapper.toResponseDto(task)
    }

    private suspend fun getFilteredAndSortedMembers(
        selectedTask: TaskResponseDTO,
        NonfilterByLevel: Boolean
    ): List<TeamMemberResponseDTO> =
        withContext(Dispatchers.IO) {
            filteredMembers.getSelectedMembersForTask(selectedTask, NonfilterByLevel)
                .sortedBy { it.performancesForTasks.getValue(selectedTask.taskName) }
    }

    suspend fun AssigningPairedMembersForSelectedTask(
        selectedTaskName: String,
        NonfilterByLevel: Boolean
    ): Map<String, Int> =
        withContext(Dispatchers.IO) {
            // 작업 조회
            val selectedTask = getSelectedTask(selectedTaskName)

            // 멤버 필터링 및 정렬
            val memberInfo = getFilteredAndSortedMembers(selectedTask, NonfilterByLevel)

            //{ "이프로": 4, "최프로": 3 }
            // 이름과 점수를 Pair로 묶어 Map 생성
            memberInfo.associate { member ->
                member.teamMemberName to member.performancesForTasks.getValue(selectedTask.taskName)
            }
        }

    suspend fun targetedTask(
        taskHistoryRequests: List<TasksHistoryRequest>
    ): Map<String, Int> =
        withContext(Dispatchers.IO) {
            // 작업 리스트 초기화
            val selectedTasks = mutableListOf<Task>()

            // taskHistoryRequests에서 작업을 조회하고 리스트에 추가
            taskHistoryRequests.forEach { taskHistoryRequest ->
                val task = taskRepository.findByTaskName(taskHistoryRequest.name)
                    ?: throw ResourceNotFoundException("Task with name '${taskHistoryRequest.name}' not found")
                selectedTasks.add(task)
            }

            // taskName과 difficulty를 Pair로 묶어 Map 생성
            selectedTasks.associate { task ->
                task.taskName to task.difficulty
            }
        }
}