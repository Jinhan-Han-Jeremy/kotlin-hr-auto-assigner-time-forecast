package org.github.hrautoassignertaskhoursforecast.TeamMembers.application.dto

data class TeamMemberResponseDTO(
    val id: Long,
    val teamMemberName: String,
    val teamMemberRole: String,
    val level: Int,
    val isWorking: Boolean,
    val performancesForTasks: Map<String, Int>,
    val achievementsScore: Float
) {
    override fun toString(): String {
        return "TeamMemberResponseDTO(id=$id, name='$teamMemberName', role='$teamMemberRole', performances=$performancesForTasks)"
    }
}