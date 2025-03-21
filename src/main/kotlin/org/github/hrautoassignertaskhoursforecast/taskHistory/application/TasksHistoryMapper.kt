package org.github.hrautoassignertaskhoursforecast.taskHistory.application

import org.github.hrautoassignertaskhoursforecast.task.domain.entity.vo.EmployeeRoles
import org.github.hrautoassignertaskhoursforecast.taskHistory.application.dto.TasksHistoryRequest
import org.github.hrautoassignertaskhoursforecast.taskHistory.application.dto.TasksHistoryResponse
import org.github.hrautoassignertaskhoursforecast.taskHistory.domain.entity.TasksHistory
import org.github.hrautoassignertaskhoursforecast.taskHistory.domain.vo.TeamMembersVO
import org.github.hrautoassignertaskhoursforecast.global.TaskState
import org.springframework.stereotype.Component

@Component
class TasksHistoryMapper {
    fun toEntity(request: TasksHistoryRequest): TasksHistory {
        return TasksHistory(
            name = request.name,
            teamMembers = request.teamMembers?.let { TeamMembersVO(it) },
            employeeRoles = EmployeeRoles(request.employeeRoles.joinToString(",")),
            spendingDays = request.spendingDays,
            expectedDays = request.expectedDays,
            state = TaskState.from(request.state.value),
            requirementsSatisfied = request.requirementsSatisfied,
            startedAt = request.startedAt,
            endedAt = request.endedAt
        )
    }

    fun toResponse(entity: TasksHistory): TasksHistoryResponse {
        return TasksHistoryResponse(
            id = entity.id,
            name = entity.name,
            teamMembers = entity.teamMembers?.membersNames,
            employeeRoles = entity.employeeRoles.roles.map { it.displayName },
            spendingDays = entity.spendingDays,
            expectedDays = entity.expectedDays,
            state = entity.state,
            requirementsSatisfied = entity.requirementsSatisfied,
            startedAt = entity.startedAt,
            endedAt = entity.endedAt
        )
    }
}