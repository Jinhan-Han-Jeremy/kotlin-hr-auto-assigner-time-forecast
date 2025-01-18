package org.github.hrautoassignertaskhoursforecast.WorkStream.application.dto

import org.github.hrautoassignertaskhoursforecast.Task.domain.entity.vo.EmployeeRoles

data class TaskPairDto(
    val id: Long = 0L, // 기본값 또는 예외 처리,
    val name: String
//    val employeeRoles: EmployeeRoles
)