package org.github.hrautoassignertaskhoursforecast.teamMembers.application.dto

data class TeamMemberSearchDTO(
    val teamMemberName: String? = null,
    val teamMemberRole: String? = null,
    val level: Int? = null,
    val isWorking: Boolean? = null,
    val achievementsScore: Float? = null
)
