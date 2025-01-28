package org.github.hrautoassignertaskhoursforecast.workStream.application.dto

data class WorkStreamResponse(
    val id: Long,
    val workstream: String,
    val availableJobs: String
)