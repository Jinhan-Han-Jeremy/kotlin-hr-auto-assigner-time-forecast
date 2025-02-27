package org.github.hrautoassignertaskhoursforecast.taskHistory.application.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.github.hrautoassignertaskhoursforecast.taskHistory.infrastructure.jdbc.TasksHistoryRepository
import org.github.hrautoassignertaskhoursforecast.taskHistory.domain.vo.TeamMembersConverter
import org.github.hrautoassignertaskhoursforecast.teamMembers.application.service.TeamMemberService
import org.github.hrautoassignertaskhoursforecast.teamMembers.infrastructure.jdbc.TeamMemberRepository
import org.springframework.stereotype.Service


@Service
class TasksHistoryManipulator(
    private val tasksHistoryRepository: TasksHistoryRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val teamMemberService: TeamMemberService
) {


    //임시 멤버 정보 데이터
    data class MemberDataWithTask(
        val singleName: String,
        val historyName: String, // 해당 멤버이름이 포함된 TasksHistory
        val expectedScore: Double,   // 업무 완수에 걸린 예측 시간
        var recordedScore: Double // 업무 완수에 걸린 측정 시간
    )

    suspend fun getMemberScoresWithHistory(memberNames: List<String>, historyName: String): List<MemberDataWithTask> =
        withContext(Dispatchers.IO) {
            memberNames.map { memberName ->

                // 작업 점수 계산
                val score = teamMemberService.getScoreForTask(memberName, historyName)

                // scores의 각 점수를 MemberData로 변환
                MemberDataWithTask(
                    singleName = memberName,
                    historyName = historyName,
                    expectedScore = score,
                    recordedScore = 0.0
                )

            }
        }

    //팀원들의 점수로 속도를 변환 EX) 1.0 / 2.0 + 1.0 / 3.0
    suspend fun getTotalCalculatedExpectedTime(membersData: List<MemberDataWithTask>): Double {
        // MemberData에서 속도 값 리스트를 추출
        val speedList = convertMemberDataToSpeeds(membersData)
        // 추출한 속도 값 리스트의 총합 계산
        val totalSpeed = getTotalSpeed(speedList)
        return if (totalSpeed > 0) 1.0 / totalSpeed else 0.0
    }
    //각 MemberData의 expectedScore를 개별 속도(Double)로 변환하는 함수
    private fun convertMemberDataToSpeeds(allData: List<MemberDataWithTask>): List<Double> {
        return allData.map { member ->
            if (member.expectedScore > 0) 1.0 / member.expectedScore else 0.0
        }
    }

    //팀원들의 점수로 속도를 변환 EX) 1.0 / 2.0 + 1.0 / 3.0
    private suspend fun getTotalSpeedOfMembers(allData: List<MemberDataWithTask>): Double {
        val speeds = convertMemberDataToSpeeds(allData)
        return getTotalSpeed(speeds)
    }

    // 개인 비율 계산 함수
    private suspend fun calculateRatio(personalSpeed: Double, membersData: List<MemberDataWithTask>): Double =
        withContext(Dispatchers.IO) {
            val totalSpeed = getTotalSpeedOfMembers(membersData)
            if (totalSpeed > 0) personalSpeed / totalSpeed else 0.0
        }

    suspend fun updateAchievementsScores(
        allData: List<MemberDataWithTask>,
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

    // 속도 리스트(List<Double>)의 총합을 계산하는 함수
    suspend fun getTotalSpeed(expectedSpeeds: List<Double>): Double{
        return expectedSpeeds.sum()
    }

    // 개인 속도 계산 함수
    suspend fun getCalculatePersonalSpeed(expectedScore: Double): Double {
            if (expectedScore > 0)
                return (1.0 / expectedScore)
            else
                return 9999.0
        }

    //실제 기록된 소요 시간
    suspend fun getRecordedHistoryTime(historyName: String): Double =
        withContext(Dispatchers.IO) {
            tasksHistoryRepository.findByName(historyName).spendingDays?.toDouble() ?: 0.0
    }

    //실제 기록된 소요 시간
    suspend fun getExpectedHistoryTime(historyName: String): Double =
        withContext(Dispatchers.IO) {
            tasksHistoryRepository.findByName(historyName).expectedDays?.toDouble() ?: 0.0
        }

    //실제 소요 시간과 이론적 소요 시간의 차이를 계산
    fun getCalculatedDifference(actualTime: Double, expectedTime: Double): Double {
        return actualTime - expectedTime
    }


}