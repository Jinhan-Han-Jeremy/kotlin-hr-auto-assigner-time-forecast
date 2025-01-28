package org.github.hrautoassignertaskhoursforecast.workStream.application.dto

data class WorkStreamRequest(
    val workstream: String,
    val availableJobs: String
)