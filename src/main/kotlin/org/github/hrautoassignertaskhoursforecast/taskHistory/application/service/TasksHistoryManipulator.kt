package org.github.hrautoassignertaskhoursforecast.taskHistory.application.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.github.hrautoassignertaskhoursforecast.taskHistory.infrastructure.jdbc.TasksHistoryRepository
import org.github.hrautoassignertaskhoursforecast.taskHistory.domain.vo.TeamMembersConverter
import org.github.hrautoassignertaskhoursforecast.teamMembers.application.service.TeamMemberService
import org.github.hrautoassignertaskhoursforecast.teamMembers.infrastructure.jdbc.TeamMemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class TasksHistoryManipulator(
    private val tasksHistoryRepository: TasksHistoryRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val teamMemberService: TeamMemberService
) {

    @Transactional
    suspend fun updateTeamMemberAchievementsScore(historyName: String) {

        val teamMembersNames = getTeamMemberNamesFromHistoryName(historyName)
        val splittedNamesList = parseTeamMemberNames(teamMembersNames)
        val memberData = getMemberScoresWithHistory(splittedNamesList, historyName)
        print("memberData.size() " + memberData.size + memberData.toString())
        val actualTime = getRecordedHistoryTime(historyName)     //실제 소요 시간 가져오기
        val expectedTime = getTotalCalculatedExpectedTime(memberData)    //이론적 소요 시간을 계산
        val totalDifference = getCalculatedDifference(actualTime, expectedTime)  //실제 소요 시간과 이론적 소요 시간의 차이

        updateAchievementsScores(memberData, totalDifference)
    }

    //팀원 이름들 파싱
    private suspend fun getTeamMemberNamesFromHistoryName(historyName: String): String=
        withContext(Dispatchers.IO){

        val taskHistory = tasksHistoryRepository.findByName(historyName)

        taskHistory.teamMembers?.membersNames?.joinToString(",").toString()
    }

    //팀원 이름들 파싱
    private fun parseTeamMemberNames(teamMembersNames: String): List<String> {
        print("teamMembersNames "+teamMembersNames)
        val teamMembersVO = TeamMembersConverter().convertToEntityAttribute(teamMembersNames)

        return teamMembersVO?.membersNames ?: emptyList()
    }

    //임시 멤버 정보 데이터
    data class MemberData(
        val singleName: String,
        val historyName: String, // 해당 멤버이름이 포함된 TasksHistory
        val expectedScore: Double,   // 업무 완수에 걸린 예측 시간
        var recordedScore: Double // 업무 완수에 걸린 측정 시간
    )

    private suspend fun getMemberScoresWithHistory(memberNames: List<String>, historyName: String): List<MemberData> =
        withContext(Dispatchers.IO) {
            memberNames.map { memberName ->

                // 작업 점수 계산
                val score = teamMemberService.getScoreForTask(memberName, historyName)

                // scores의 각 점수를 MemberData로 변환
                MemberData(
                    singleName = memberName,
                    historyName = historyName,
                    expectedScore = score,
                    recordedScore = 0.0
                )

            }
        }

    //팀원들의 점수로 속도를 변환 EX) 1.0 / 2.0 + 1.0 / 3.0
    private suspend fun getTotalSpeeddOfMembers(allData: List<MemberData>): Double =
        withContext(Dispatchers.IO) {
        allData.sumOf { member ->
            if (member.expectedScore > 0) 1.0 / member.expectedScore else 0.0
        }
    }

    // 개인 속도 계산 함수
    private suspend fun getCalculatePersonalSpeed(expectedScore: Double): Double =
        withContext(Dispatchers.IO) {
            if (expectedScore > 0) 1.0 / expectedScore else 0.0
        }

    // 개인 비율 계산 함수
    private suspend fun calculateRatio(personalSpeed: Double, membersData: List<MemberData>): Double =
        withContext(Dispatchers.IO) {
            val totalSpeed = getTotalSpeeddOfMembers(membersData)
            if (totalSpeed > 0) personalSpeed / totalSpeed else 0.0
    }

    //이론적 소요 시간을 계산
    private suspend fun getTotalCalculatedExpectedTime(membersData: List<MemberData>): Double {

        val totalSpeed = getTotalSpeeddOfMembers(membersData)
        return if (totalSpeed > 0) 1.0 / totalSpeed else 0.0
    }

    //실제 기록된 소요 시간
    private suspend fun getRecordedHistoryTime(historyName: String): Double =
        withContext(Dispatchers.IO) {
            tasksHistoryRepository.findByName(historyName).spendingDays?.toDouble() ?: 0.0
    }

    //실제 기록된 소요 시간
    private suspend fun getExpectedHistoryTime(historyName: String): Double =
        withContext(Dispatchers.IO) {
            tasksHistoryRepository.findByName(historyName).expectedDays?.toDouble() ?: 0.0
        }

    //실제 소요 시간과 이론적 소요 시간의 차이를 계산
    private fun getCalculatedDifference(actualTime: Double, expectedTime: Double): Double {
        return actualTime - expectedTime
    }

    private suspend fun updateAchievementsScores(
        allData: List<MemberData>,
        totalDifference: Double
    ) {
        withContext(Dispatchers.IO) {
            allData.forEach { member ->
                print("member "+member.historyName + " "+member.singleName + " "+member.expectedScore)
                val personalSpeed = getCalculatePersonalSpeed(member.expectedScore)
                val ratio = calculateRatio(personalSpeed, allData)
                val personalDelay = totalDifference * ratio

                print("personalSpeed " + personalSpeed)

                print("personalDelay "+ personalDelay)
                val teamMember = teamMemberRepository.findByTeamMemberName(member.singleName)
                teamMember.achievementsScore += personalDelay.toFloat()

                teamMemberRepository.save(teamMember)
            }
        }
    }

}