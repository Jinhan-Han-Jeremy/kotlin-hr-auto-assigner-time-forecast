package org.github.hrautoassignertaskhoursforecast.analyzingCalculator.application.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.github.hrautoassignertaskhoursforecast.analyzingCalculator.application.dto.AssignedTeamsForTasksDto
import org.github.hrautoassignertaskhoursforecast.analyzingCalculator.application.dto.analyzedResultsResponse
import org.github.hrautoassignertaskhoursforecast.global.FastApiService
import org.github.hrautoassignertaskhoursforecast.teamMembers.application.service.FilteredMembers
import org.github.hrautoassignertaskhoursforecast.global.milp.MilpResponse
import org.github.hrautoassignertaskhoursforecast.task.application.service.TaskService
import org.github.hrautoassignertaskhoursforecast.taskHistory.application.dto.TasksHistoryResponse
import org.github.hrautoassignertaskhoursforecast.taskHistory.application.service.TasksHistoryManipulator
import org.github.hrautoassignertaskhoursforecast.taskHistory.application.service.TasksHistoryService
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class TaskTimeForecastService(
    private val filteredMembers: FilteredMembers,
    private val taskService: TaskService,
    private val fastApiService: FastApiService,
    private val tasksHistoryManipulator: TasksHistoryManipulator,
    private val tasksHistoryService: TasksHistoryService
){

    suspend fun AssigningPairedMembersForSelectedTask(
        selectedTaskName: String,
        //NonfilterByLevel이 true면 작업별 difficulty 레벨에 따른 팀원 추출
        NonfilterByLevel: Boolean
    ): Map<String, Int> =
        withContext(Dispatchers.IO) {

            // 작업 조회
            val selectedTask = taskService.findTaskByName(selectedTaskName)

            // 멤버 필터링 및 정렬
            val memberInfo = filteredMembers.getFilteredAndSortedMembers(selectedTask, NonfilterByLevel)

            //{ "이프로": 4, "최프로": 3 }
            // 이름과 점수를 Pair로 묶어 Map 생성
            memberInfo.associate { member ->
                member.teamMemberName to member.performancesForTasks.getValue(selectedTask.taskName)
            }
        }


    //작업들에 대한 모든 작업 가능 인원들의 집합 { "이프로": 4},{ "최프로": 3 }
    private suspend fun getFilteredMembersWithPerformances(selectedTaskNames: List<String>, NonfilterByLevel: Boolean) :
            List<Map<String, Int>>{
        val memberPerformances = mutableListOf<Map<String, Int>>()

        selectedTaskNames.forEach {selectedTaskName ->
            //작업 이름들에 대하여  작업 가능한 작업자들의 작업 수치{ "이프로": 4, "최프로": 3 }
            val result = AssigningPairedMembersForSelectedTask(selectedTaskName, NonfilterByLevel)
            memberPerformances.add(result)
        }
        return memberPerformances
    }

    // 멤버들의 수행 가능 정보를 가져와 가공하는 함수
    private suspend fun prepareMemberPerformances(
        selectedTaskNames: List<String>, NonfilterByLevel: Boolean
    ): Pair<MutableList<MutableMap<String, Int>>, MutableList<String>> {
        val memberPerformances = getFilteredMembersWithPerformances(selectedTaskNames, NonfilterByLevel)
            .map { it.toMutableMap() } // 내부 Map도 Mutable로 변환
            .toMutableList() // 리스트도 Mutable로 변환

        val appliedTaskNames = selectedTaskNames.toMutableList()
        return Pair(memberPerformances, appliedTaskNames)
    }

    // 중복 멤버 제거 함수
    private fun removeDuplicateMembers(
        index: Int,
        memberPerformances: MutableList<MutableMap<String, Int>>,
        appliedTaskNames: MutableList<String>,
        teamMembers: List<String>
    ) {
        //모든 리스트에 중복이름 제거
        for (offset in 1..2) { // index+1, index+2를 반복적으로 처리
            val nextIndex = index + offset
            if (nextIndex < memberPerformances.size) {
                memberPerformances[nextIndex].keys.removeAll(teamMembers)
                if (memberPerformances[nextIndex].isEmpty()) {
                    appliedTaskNames.removeAt(nextIndex)
                }
            }
        }
    }

    // 작업을 멤버들에게 할당하고 결과 리스트를 반환하는 함수
    suspend fun resultsPriortizedByDifficulty(
        selectedTaskNames: List<String>, NonfilterByLevel: Boolean
    ): List<AssignedTeamsForTasksDto> {
        //difficulty를 기준으로 작업이름을 배열
        val finalTaskNamesToAssign = taskService.targetedTaskNamesInTasksHistory(selectedTaskNames)

        val (memberPerformances, appliedTaskNames) = prepareMemberPerformances(finalTaskNamesToAssign, NonfilterByLevel)
        val responseList = mutableListOf<AssignedTeamsForTasksDto>()

        appliedTaskNames.forEachIndexed { index, selectedTaskName ->
            val membersMap = memberPerformances.getOrNull(index) ?: emptyMap()

            // 최대 3명까지 선택
            val teamMembers = membersMap.keys.take(3).toList()
            val values = membersMap.values.take(3).toList()

            // 중복 멤버 제거 함수 호출
            removeDuplicateMembers(index, memberPerformances, appliedTaskNames, teamMembers)

            // 평균 수행 시간 계산
            val speedValues: List<Double> = values.map {
                tasksHistoryManipulator.getCalculatePersonalSpeed(it.toDouble())
            }

            val totalSpeed = tasksHistoryManipulator.getTotalSpeed(speedValues)
            val duration = 1.0 / totalSpeed

            // 결과 리스트에 추가
            responseList.add(
                AssignedTeamsForTasksDto(
                    taskName = selectedTaskName,
                    teamMembers = teamMembers,
                    duration = duration
                )
            )
        }

        return responseList
    }


//각 멤버가 마지막으로 등장한 위치를 기록하는 함수
    private fun updateLastSeen(
        memberPerformances: List<MutableMap<String, Int>>
    ): MutableMap<String, Int> {
        val lastSeen = mutableMapOf<String, Int>() // 각 멤버가 마지막으로 등장한 위치 저장

        for (i in memberPerformances.indices) {
            for (member in memberPerformances[i].keys) {
                lastSeen[member] = i // 해당 멤버의 마지막 위치 업데이트
            }
        }

        return lastSeen
    }

    // 중복 멤버 제거 및 appliedTaskNames 정리하는 함수
    private fun removeDuplicatesAndCleanTasks(
        memberPerformances: MutableList<MutableMap<String, Int>>,
        appliedTaskNames: MutableList<String>,
        lastSeen: MutableMap<String, Int>
    ) {
        val toRemoveIndices = mutableSetOf<Int>() // 삭제할 작업 인덱스 저장

        for (i in memberPerformances.indices) {
            val currentMap = memberPerformances[i]
            val keysToRemove = mutableListOf<String>()

            // 마지막 위치가 아닌 멤버를 제거 후보에 추가
            for (member in currentMap.keys) {
                if (lastSeen[member] != i) {
                    keysToRemove.add(member)
                }
            }

            // 최소 한 명의 멤버가 남도록 보장
            if (keysToRemove.size < currentMap.size) {
                keysToRemove.forEach { currentMap.remove(it) }
            }

            // 맵이 비어 있으면 삭제 후보에 추가
            if (currentMap.isEmpty()) {
                toRemoveIndices.add(i)
            }
        }

        // appliedTaskNames에서 안전하게 삭제 (리스트 크기 변화 고려하여 역순 삭제)
        toRemoveIndices.sortedDescending().forEach { index ->
            if (index < appliedTaskNames.size) {
                appliedTaskNames.removeAt(index)
            }
        }
    }

    // //최소 한명은 각 작업에 존재하며 중복 멤버 제거 함수
    private fun removeDuplicateMembers(
        memberPerformances: MutableList<MutableMap<String, Int>>,
        appliedTaskNames: MutableList<String>
    ) {
        val assignedMembers = mutableSetOf<String>() // 첫 번째 작업에 할당된 멤버 저장

        val toRemoveIndices = mutableSetOf<Int>() // 삭제할 작업 인덱스 저장

        for (i in memberPerformances.indices) {
            val currentMap = memberPerformances[i]
            val keysToRemove = mutableListOf<String>()

            if (i == 0) { // 첫 번째 작업
                assignedMembers.addAll(currentMap.keys) // 첫 번째 작업의 멤버를 저장
            } else { // 이후 작업들
                for (member in currentMap.keys) {
                    if (assignedMembers.contains(member)) { // 첫 번째 작업에 배정된 멤버 제거
                        keysToRemove.add(member)
                    }
                }
            }

            println("Before Removal: $currentMap") // 🔥 디버깅용
            keysToRemove.forEach { currentMap.remove(it) }
            println("After Removal: $currentMap")  // 🔥 디버깅용

            if (currentMap.isEmpty()) {
                toRemoveIndices.add(i)
            }
        }

        // appliedTaskNames에서도 제거
        toRemoveIndices.sortedDescending().forEach { index ->
            if (index < appliedTaskNames.size) {
                appliedTaskNames.removeAt(index)
            }
        }
    }


    suspend fun milpToAnalyzedResults(selectedTaskNames: List<String>, NonfilterByLevel: Boolean) : List<AssignedTeamsForTasksDto> {
        // MILP 결과를 저장할 변수 선언
        var milpResults: MilpResponse? = null
        val convertedMilpResults = mutableListOf<AssignedTeamsForTasksDto>()

        //difficulty를 기준으로 작업이름을 배열
        val finalTaskNamesToAssign = taskService.targetedTaskNamesInTasksHistory(selectedTaskNames)

        // 작업들에 대한 모든 작업 가능 인원들의 집합 [{최덕마=3}, {최덕마=4, 최비례=4}]
        val memberPerformances = getFilteredMembersWithPerformances(finalTaskNamesToAssign, NonfilterByLevel)

        print("memberPerformances format:" + memberPerformances)


        val mutableMemberPerformances = memberPerformances.map { it.toMutableMap() }.toMutableList()

        val mutableTasksNamesToAssign = finalTaskNamesToAssign.toMutableList()

        removeDuplicateMembers(mutableMemberPerformances , mutableTasksNamesToAssign)

        print("mutable memberPerforms :" + mutableMemberPerformances)

        // 멤버 개수가 1개 이상이면 MILP 적용
        if (mutableMemberPerformances.size > 1 ) {

            milpResults = fastApiService.resultsAppliedByMilp(mutableMemberPerformances, mutableTasksNamesToAssign)

            //MILP 결과를 AssignedTeamsForTasksResponse 형태로 변환하여 저장
            milpResults.tasks.forEachIndexed { index, task ->
                val teamMembers = milpResults.teams.getOrNull(index) ?: emptyList()
                val duration = milpResults.durations.getOrNull(index) ?: 0.0
                convertedMilpResults.add(AssignedTeamsForTasksDto(task, teamMembers, duration))
            }
        }

        return convertedMilpResults
    }

    suspend fun analyzeForecast(selectedTaskNames: List<String>, nonFilterByLevel: Boolean): analyzedResultsResponse {
        // 어려운 작업을 우선 배치하는 Greedy 기반 분석
        val results = resultsPriortizedByDifficulty(selectedTaskNames, nonFilterByLevel)

        println("forecastService results $results")

        // MILP 분석 결과 생성
        val milpResults = when{selectedTaskNames.size > 1 -> milpToAnalyzedResults(selectedTaskNames, nonFilterByLevel)
            else ->emptyList()
        }

        // 응답 객체 생성
        return analyzedResultsResponse(
            resultsPriortizedByDifficulty = results,
            milpToAnalyzedResults = milpResults
        )
    }

    suspend fun assignExpectedTaskHistories
                (selectedTaskNames: List<String>,
                 assignmentMethod: Int,
                 nonFilterByLevel: Boolean,
                 startedAt: LocalDate?)
    : List<AssignedTeamsForTasksDto> {

        return when {
            selectedTaskNames.size > 1 && assignmentMethod == 2 -> {
                milpToAnalyzedResults(selectedTaskNames, nonFilterByLevel)
            }
            assignmentMethod == 1 -> {
                resultsPriortizedByDifficulty(selectedTaskNames, nonFilterByLevel)
            }
            else -> {
                println("선택한 작업이 1개 이하이므로 resultsPriortizedByDifficulty로 분석 예측")
                resultsPriortizedByDifficulty(selectedTaskNames, nonFilterByLevel)
            }
        }
    }

    //예측한거에 따라서 업데이트하거나 없다면 새로운 TaskHistory 생성
    suspend fun updateOrCreateTaskHistory(
        selectedTaskNames: List<String>,
        assignmentMethod: Int,
        nonFilterByLevel: Boolean,
        startedAt: LocalDate?
    ): List<TasksHistoryResponse> = coroutineScope {

        val expectedResults = assignExpectedTaskHistories(selectedTaskNames, assignmentMethod, nonFilterByLevel, startedAt)

        val responses = mutableListOf<TasksHistoryResponse>()

        expectedResults.forEach{expectedResult ->
            val rolesToUpdate = taskService.findTaskByName(expectedResult.taskName).employeeRoles
            val updatedHistory = tasksHistoryService.createOrUpdateTasksHistory(expectedResult, rolesToUpdate, startedAt)

            responses.add(updatedHistory)
        }


    // 업데이트된 기존 TaskHistory + 새로 생성된 TaskHistory 반환
    return@coroutineScope responses
    }

}