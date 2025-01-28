package org.github.hrautoassignertaskhoursforecast.web

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.github.hrautoassignertaskhoursforecast.Task.application.TaskMapper
import org.github.hrautoassignertaskhoursforecast.Task.infrastructure.jdbc.TaskRepository
import org.github.hrautoassignertaskhoursforecast.TeamMembers.application.dto.TeamMemberRequestDTO
import org.github.hrautoassignertaskhoursforecast.TeamMembers.application.dto.TeamMemberResponseDTO
import org.github.hrautoassignertaskhoursforecast.TeamMembers.application.dto.TeamMemberSearchDTO
import org.github.hrautoassignertaskhoursforecast.TeamMembers.application.service.FilteredMembers
import org.github.hrautoassignertaskhoursforecast.TeamMembers.application.service.TeamMemberService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.github.hrautoassignertaskhoursforecast.TeamMembers.application.service.TeamMemberRankingService
import org.springframework.http.HttpStatus

@RestController
@RequestMapping("/team-members")
class TeamMemberController(
    private val teamMemberService: TeamMemberService,
    private val filteredMembers: FilteredMembers,
    private val teamMemberRankingService: TeamMemberRankingService,
    private val taskRepository: TaskRepository,
    private val taskMapper: TaskMapper
) {

    @GetMapping
    suspend fun getAllTeamMembers(): ResponseEntity<List<TeamMemberResponseDTO>> {
        val members = teamMemberService.findAllTeamMembers()
        return ResponseEntity.ok(members)
    }

    @PostMapping
    suspend fun createTeamMember(@RequestBody request: TeamMemberRequestDTO): ResponseEntity<TeamMemberResponseDTO> {
        val createdMember = teamMemberService.createTeamMember(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMember)
    }

    @PutMapping("/{id}")
    suspend fun updateTeamMember(@PathVariable id: Long, @RequestBody request: TeamMemberRequestDTO): ResponseEntity<TeamMemberResponseDTO> {
        val updatedMember = teamMemberService.updateTeamMember(id, request)
        return ResponseEntity.ok(updatedMember)
    }

    @DeleteMapping("/{id}")
    suspend fun deleteTeamMember(@PathVariable id: Long): ResponseEntity<Void> {
        teamMemberService.deleteTeamMember(id)
        return ResponseEntity.noContent().build()
    }


    @GetMapping("/{id}")
    suspend fun getTeamMemberById(@PathVariable id: Long): ResponseEntity<TeamMemberResponseDTO> {
        val member = teamMemberService.findTeamMemberById(id)
        return ResponseEntity.ok(member)
    }


    @GetMapping("/search/name")
    suspend fun getTeamMemberByName(@RequestParam name: String): ResponseEntity<TeamMemberResponseDTO> {
        val member = teamMemberService.findTeamMemberByName(name)
        return ResponseEntity.ok(member)
    }

    @GetMapping("/search")
    suspend fun searchTeamMembers(@ModelAttribute inputData: TeamMemberSearchDTO): ResponseEntity<List<TeamMemberSearchDTO>> {
        val members = teamMemberService.searchTeamMembers(inputData)

        // TeamMemberSearchDTO 리스트로 변환
        val response = members.map { member ->
            TeamMemberSearchDTO(
                teamMemberName = member.teamMemberName,
                teamMemberRole = member.teamMemberRole,
                level = member.level,
                isWorking = member.isWorking,
                achievementsScore = member.achievementsScore
            )
        }

        return ResponseEntity.ok(response)
    }

    @GetMapping("/available")
    suspend fun getAvailableTeamMembers(): ResponseEntity<List<TeamMemberSearchDTO>>{
        val members = filteredMembers.getUnassigningTeamMembers(available = true)

        // TeamMemberSearchDTO 리스트로 변환
        val response = members.map { member ->
            TeamMemberSearchDTO(
                teamMemberName = member.teamMemberName,
                teamMemberRole = member.teamMemberRole,
                level = member.level,
                isWorking = true,
                achievementsScore = member.achievementsScore
            )
        }

        return ResponseEntity.ok(response)
    }

    @GetMapping("/unavailable")
    suspend fun getUnavailableTeamMembers(): ResponseEntity<List<TeamMemberSearchDTO>> {

        val members= filteredMembers.getUnassigningTeamMembers(available = false)

        // TeamMemberSearchDTO 리스트로 변환
        val response = members.map { member ->
            TeamMemberSearchDTO(
                teamMemberName = member.teamMemberName,
                teamMemberRole = member.teamMemberRole,
                level = member.level,
                isWorking = false,
                achievementsScore = member.achievementsScore
            )
        }

        return ResponseEntity.ok(response)
    }

    @GetMapping("/selectedtask/{taskId}")
    suspend fun getSelectedTeamMembersForTask(
        @PathVariable taskId: Long,
        @RequestParam NonFilterByLevel: Boolean
    ): ResponseEntity<List<Map<String, Any>>> {
        val task = withContext(Dispatchers.IO) {
            taskRepository.findById(taskId).orElseThrow {
                NoSuchElementException("Selected Task not found with id: $taskId")
            }
        }

        val selectedTask = taskMapper.toResponseDto(task)
        val memberInfo = filteredMembers.getSelectedMembersForTask(selectedTask, NonFilterByLevel)

        val filteredResponse = memberInfo.map { member ->
            mapOf(
                "id" to member.id,
                "teamMemberName" to member.teamMemberName,
                "selectedTask" to selectedTask.taskName,
                "role" to member.teamMemberRole,
                "level" to member.level,
                "achievementsScore" to member.achievementsScore,
            )
        }

        return ResponseEntity.ok(filteredResponse)
    }

    /**
     * 월간 상위 3명의 팀원 성과 조회
     */
    @GetMapping("/monthly/top3")
    fun getTop3Monthly(): ResponseEntity<List<TeamMemberResponseDTO>> {
        val top3Monthly = teamMemberRankingService.getTop3AchievementsMonthly()
        return ResponseEntity.ok(top3Monthly)
    }

    @PatchMapping("/{id}/performancesfortasks")
    suspend fun addPerformanceForTask(@PathVariable id: Long, @RequestBody newPerformances: Map<String, Int>): ResponseEntity<TeamMemberResponseDTO> {
        val updatedMember = teamMemberService.addPerformanceForTask(id, newPerformances)
        return ResponseEntity.ok(updatedMember)
    }

    @PatchMapping("/{id}/scorestask")
    suspend fun updateScoresForTasks(@PathVariable member_id: Long, @RequestParam type: String, @RequestParam score: Int): ResponseEntity<TeamMemberResponseDTO> {
        val updatedMember = teamMemberService.updateScoresForTasks(member_id, type, score)
        return ResponseEntity.ok(updatedMember)
    }

    @PatchMapping("/{id}/performance-name")
    suspend fun updateNameForPerformance(@PathVariable id: Long, @RequestParam type: String, @RequestParam newTypeName: String): ResponseEntity<TeamMemberResponseDTO> {
        val updatedMember = teamMemberService.updateNameForPerformance(id, type, newTypeName)
        return ResponseEntity.ok(updatedMember)
    }

    @PatchMapping("/{id}/renew-performances")
    suspend fun renewPerformancesForTasks(@PathVariable id: Long, @RequestBody newPerformances: Map<String, Int>): ResponseEntity<TeamMemberResponseDTO> {
        val updatedMember = teamMemberService.renewPerformancesForTasks(id, newPerformances)
        return ResponseEntity.ok(updatedMember)
    }

}