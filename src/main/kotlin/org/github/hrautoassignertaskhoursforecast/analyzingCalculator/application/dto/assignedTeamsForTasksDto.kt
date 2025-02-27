package org.github.hrautoassignertaskhoursforecast.analyzingCalculator.application.dto

data class AssignedTeamsForTasksResponse(
    val taskName: String,
    val teamMembers: List<String>,
    val duration: Double
)