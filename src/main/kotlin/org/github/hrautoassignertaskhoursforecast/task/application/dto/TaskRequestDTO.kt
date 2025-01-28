package org.github.hrautoassignertaskhoursforecast.task.application.dto

import org.github.hrautoassignertaskhoursforecast.global.RoleType

data class TaskRequestDTO(
    val taskName: String,
    val difficulty: Int,
    val employeeRoles: List<String>,
    val requirements: List<String>
) {
    fun toRoleTypes(): List<RoleType> {
        return employeeRoles.map { roleName ->
            RoleType.values().find { it.displayName.equals(roleName, ignoreCase = true) }
                ?: throw IllegalArgumentException("Invalid role: $roleName")
        }
    }
}