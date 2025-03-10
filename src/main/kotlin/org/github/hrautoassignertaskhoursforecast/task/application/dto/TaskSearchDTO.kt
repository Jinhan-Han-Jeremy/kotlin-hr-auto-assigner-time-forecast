package org.github.hrautoassignertaskhoursforecast.task.application.dto

data class TaskSearchDTO (
    val taskName: String? = null,
    val difficulty: Int? = null,

    // VO가 아니라 단순히 배열 형태로 받을 경우
    val employeeRoles: List<String>? = null,

    // 마찬가지로 requirements도 배열 형태로
    val requirements: List<String>? = null
)