package org.github.hrautoassignertaskhoursforecast.taskHistory.application.dto

import org.github.hrautoassignertaskhoursforecast.global.TaskState
import java.time.LocalDate

data class TasksHistoryRequest(
    val name: String,
    val teamMembers: List<String>? = null, // 팀원 이름 리스트
    val employeeRoles: List<String> = emptyList(), // 직무 리스트
    val spendingDays: Float? = null,
    val expectedDays: Float? = null,
    val state: TaskState,
    val requirementsSatisfied: Boolean? = null,
    val startedAt: LocalDate? = null,
    val endedAt: LocalDate? = null
)