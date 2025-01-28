package org.github.hrautoassignertaskhoursforecast.teamMembers.application.dto

data class TeamMemberRequestDTO(
    val teamMemberName: String,
    val role: String?,
    val level: Int?,
    val isWorking: Boolean?,
    val performancesForTasks: Map<String, Int>?,
    val achievementsScore: Float?
)