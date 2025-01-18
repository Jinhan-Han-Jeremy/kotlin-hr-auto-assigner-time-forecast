package org.github.hrautoassignertaskhoursforecast.TeamMembers.application.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.github.hrautoassignertaskhoursforecast.Task.application.dto.TaskResponseDTO
import org.github.hrautoassignertaskhoursforecast.Task.domain.entity.Task
import org.github.hrautoassignertaskhoursforecast.TeamMembers.application.TeamMemberMapper
import org.github.hrautoassignertaskhoursforecast.TeamMembers.application.dto.TeamMemberResponseDTO
import org.github.hrautoassignertaskhoursforecast.TeamMembers.infrastructure.jdbc.TeamMemberRepository
import org.github.hrautoassignertaskhoursforecast.TeamMembers.domain.entity.TeamMember
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Transactional(readOnly = true)
@Service
class FilteredMembers(
    private val teamMemberRepository: TeamMemberRepository,
    private val teamMemberMapper: TeamMemberMapper
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun getUnassigningTeamMembers(available: Boolean): List<TeamMemberResponseDTO> =
        withContext(Dispatchers.IO) {
        val members = teamMemberRepository.findAllByIsWorking(available)
        // 멤버 객체 로그 확인
        members.forEach { member ->
            logger.debug("DB Data - ID: ${member.id}")
            logger.debug("DB Data - Name: ${member.teamMemberName}")
            logger.debug("DB Data - Performances: ${member.performancesForTasks}")
            logger.debug("DB Data - IsWorking: ${member.isWorking}")
        }
        members.map { teamMemberMapper.toResponseDto(it) }
    }

    // 작업들에 할당 가능한 작업자들을 선출
    fun getSelectedMembersForTasks(
        members: List<TeamMember>,
        selectedTasks: List<Task>
    ): List<TeamMember> {
        return members.filter { member ->
            selectedTasks.all { task ->
                val evaluations = member.performancesForTasks.getAllPerformancesForTasks()
                val taskName = task.taskName
                val taskDifficulty = task.difficulty
                val memberLevel = member.level

                evaluations.containsKey(taskName) &&
                        isMemberLevelSuitable(taskDifficulty, memberLevel)
            }
        }
    }

    suspend fun getSelectedMembersForTask(
        selectedTask: TaskResponseDTO,
        NonFilterByLevel: Boolean
    ): List<TeamMemberResponseDTO> =
        withContext(Dispatchers.IO) {
        // 할당되지 않은 팀원 데이터를 가져오기
        val availableMembers = getUnassigningTeamMembers(true) // 반환 타입: List<TeamMemberResponseDTO>

        // DTO를 TeamMember로 변환
        val teamMembers = availableMembers.map { teamMemberMapper.toEntity(it) } // 반환 타입: List<TeamMember>

        // 팀원 필터링 및 DTO 변환: IsAssignableMembersForTask를 사용하여 필터링된 결과 반환
        val filteredMembers = IsAssignableMembersForTask(teamMembers, selectedTask, NonFilterByLevel)

        // 필터링된 멤버를 DTO로 변환하여 반환
        filteredMembers.map { teamMemberMapper.toResponseDto(it) }
    }

    // 주어진 작업에 대해 할당 가능한 팀원 목록을 반환합니다.
    private fun IsAssignableMembersForTask(
        members: List<TeamMember>,
        selectedTask: TaskResponseDTO,
        NotFilterByLevel: Boolean
    ): List<TeamMember> {
        return members.filter { member ->
            val evaluations = member.performancesForTasks.getAllPerformancesForTasks() // VO에서 실제 Map 가져오기
            val taskName = selectedTask.taskName
            val taskDifficulty = selectedTask.difficulty
            val memberLevel = member.level

            evaluations.containsKey(taskName) && when {
                NotFilterByLevel -> true // 레벨 필터링을 적용하지 않음
                else -> isMemberLevelSuitable(taskDifficulty, memberLevel)
            }
        }
    }

    // 특정 작업 난이도와 팀원 레벨로 할당 가능 여부를 확인합니다
    private fun isMemberLevelSuitable(taskDifficulty: Int, memberLevel: Int): Boolean {
        // 작업 난이도와 멤버 레벨에 따라 적합 여부를 판단
        return when {
            taskDifficulty >= 3 && memberLevel > 1 -> true // 높은 난이도에 적합한 멤버
            taskDifficulty < 3 && memberLevel < 3 -> true // 낮은 난이도에 적합한 멤버
            else -> false
        }
    }

}