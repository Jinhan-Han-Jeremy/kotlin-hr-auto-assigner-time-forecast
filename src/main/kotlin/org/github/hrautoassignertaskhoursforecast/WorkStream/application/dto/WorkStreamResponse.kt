package org.github.hrautoassignertaskhoursforecast.WorkStream.application.dto

data class WorkStreamResponse(
    val id: Long,
    val workstream: String,
    val availableJobs: String
)