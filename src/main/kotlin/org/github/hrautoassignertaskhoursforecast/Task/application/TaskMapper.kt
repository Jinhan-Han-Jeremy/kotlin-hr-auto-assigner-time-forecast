package org.github.hrautoassignertaskhoursforecast.Task.application

import org.github.hrautoassignertaskhoursforecast.Task.application.dto.TaskRequestDTO
import org.github.hrautoassignertaskhoursforecast.Task.application.dto.TaskResponseDTO
import org.github.hrautoassignertaskhoursforecast.Task.domain.entity.Task
import org.github.hrautoassignertaskhoursforecast.Task.domain.entity.vo.EmployeeRoles
import org.github.hrautoassignertaskhoursforecast.Task.domain.entity.vo.Requirements
import org.springframework.stereotype.Component

@Component
class TaskMapper {
    fun toResponseDto(entity: Task): TaskResponseDTO {
        return TaskResponseDTO(
            id = entity.id,
            taskName = entity.taskName,
            difficulty = entity.difficulty,
            employeeRoles = entity.employeeRoles.roles.map { it.displayName }, // 수정된 부분
            requirements = entity.requirements?.requirementsList ?: emptyList() // 수정된 부분
        )
    }
    /**
     * DTO -> Entity 변환
     *  - 요청 DTO에 담긴 정보로 새로운 Task 객체를 생성 */
    fun toEntity(requestDTO: TaskRequestDTO): Task {
        // EmployeeRoles 생성
        val newEmployeeRoles = EmployeeRoles()
        newEmployeeRoles.setRoles(requestDTO.toRoleTypes())

        // Requirements 생성
        val newRequirements = Requirements()
        newRequirements.setRequirements(requestDTO.requirements)

        return Task(
            taskName = requestDTO.taskName,
            difficulty = requestDTO.difficulty,
            employeeRoles = newEmployeeRoles,
            requirements = newRequirements
        )
    }
}