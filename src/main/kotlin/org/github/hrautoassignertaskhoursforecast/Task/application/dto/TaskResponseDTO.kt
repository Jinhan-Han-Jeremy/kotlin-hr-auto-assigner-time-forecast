package org.github.hrautoassignertaskhoursforecast.Task.application.dto

data class TaskResponseDTO(
    val id: Long?,
    val taskName: String,
    val difficulty: Int,
    val employeeRoles: List<String>, // RoleType의 displayName 리스트
    val requirements: List<String> // 요구사항 리스트
)