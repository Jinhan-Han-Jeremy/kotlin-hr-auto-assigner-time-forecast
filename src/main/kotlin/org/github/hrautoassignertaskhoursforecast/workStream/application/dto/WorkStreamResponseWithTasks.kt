package org.github.hrautoassignertaskhoursforecast.workStream.application.dto


data class WorkStreamResponseWithTasks(
    val id: Long,
    val workstream: String,
    val assignedTasksNames: List<String>,
    val availableJobs: String
)