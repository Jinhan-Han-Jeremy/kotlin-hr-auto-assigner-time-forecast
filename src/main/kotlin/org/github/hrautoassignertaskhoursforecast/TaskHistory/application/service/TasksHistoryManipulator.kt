package org.github.hrautoassignertaskhoursforecast.TaskHistory.application.service

import org.github.hrautoassignertaskhoursforecast.TaskHistory.infrastructure.jdbc.TasksHistoryRepository
import org.github.hrautoassignertaskhoursforecast.TaskHistory.domain.entity.TasksHistory
import org.github.hrautoassignertaskhoursforecast.TeamMembers.application.service.TeamMemberService
import org.github.hrautoassignertaskhoursforecast.TeamMembers.infrastructure.jdbc.TeamMemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class TasksHistoryManipulator(
    private val taskHistoryRepository: TasksHistoryRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val teamMemberService: TeamMemberService
) {

    @Transactional
    suspend fun updateTeamMemberAchievementsScore(teamMembersNames: String) {
        val splittedNames = parseTeamMemberNames(teamMembersNames)
        val allData = collectMemberData(splittedNames)
        val totalSpeed = calculateTotalSpeed(allData)
        val actualTime = getActualTime(allData)
        val expectedTime = calculateExpectedTime(totalSpeed)
        val difference = calculateDifference(actualTime, expectedTime)

        updateAchievementsScores(allData, totalSpeed, difference)
    }

    //팀원 이름 파싱
    private fun parseTeamMemberNames(teamMembersNames: String): List<String> {
        return teamMembersNames
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    data class MemberData(
        val singleName: String,
        val history: TasksHistory,
        val calculatedScore: Double
    )

    //팀원의 작업 히스토리와 점수를 수집
    private suspend fun collectMemberData(names: List<String>): List<MemberData> {
        return names.map { singleName ->
            val history = taskHistoryRepository.findByTeamMembers(singleName)
            val calculatedScore = teamMemberService.getScoresForTasks(singleName, history.name)
            MemberData(singleName, history, calculatedScore)
        }
    }

    //팀원들의 점수로부터 전체 속도를 계산
    private fun calculateTotalSpeed(allData: List<MemberData>): Double {
        return allData.sumOf { member ->
            if (member.calculatedScore > 0) 1.0 / member.calculatedScore else 0.0
        }
    }

    //실제 소요 시간 가져오기
    private fun getActualTime(allData: List<MemberData>): Double {
        return allData.first().history.spendingDays?.toDouble() ?: 0.0
    }

    //이론적 소요 시간을 계산
    private fun calculateExpectedTime(totalSpeed: Double): Double {
        return if (totalSpeed > 0) 1.0 / totalSpeed else 0.0
    }

    //실제 소요 시간과 이론적 소요 시간의 차이를 계산
    private fun calculateDifference(actualTime: Double, expectedTime: Double): Double {
        return actualTime - expectedTime
    }

    //팀원의 AchievementsScore 업데이트
    private suspend fun updateAchievementsScores(
        allData: List<MemberData>,
        totalSpeed: Double,
        difference: Double
    ) {
        allData.forEach { (singleName, _, calculatedScore) ->
            val personalSpeed = if (calculatedScore > 0) 1.0 / calculatedScore else 0.0
            val ratio = if (totalSpeed > 0) personalSpeed / totalSpeed else 0.0
            val personalDelay = difference * ratio

            val teamMember = teamMemberRepository.findByTeamMemberName(singleName)

            teamMember.achievementsScore = (teamMember.achievementsScore - personalDelay.toFloat())
            teamMemberRepository.save(teamMember)
        }
    }
}