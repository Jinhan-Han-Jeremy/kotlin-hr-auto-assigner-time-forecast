package org.github.hrautoassignertaskhoursforecast.analyzingCalculator.application.dto

data class AssignedTeamsForTasksDto(
    val taskName: String,
    val teamMembers: List<String>,
    val duration: Double
)