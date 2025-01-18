package org.github.hrautoassignertaskhoursforecast.WorkStream.application.dto

data class WorkStreamRequest(
    val workstream: String,
    val availableJobs: String
)