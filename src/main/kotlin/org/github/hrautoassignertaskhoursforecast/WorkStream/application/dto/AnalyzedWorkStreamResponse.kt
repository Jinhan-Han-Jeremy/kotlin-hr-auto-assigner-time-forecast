package org.github.hrautoassignertaskhoursforecast.WorkStream.application.dto

data class AnalyzedWorkStreamResponse(
    val workstream: String,
    val tasksByTrieAnalysis: List<TaskPairDto>,
    val tasksdByTextAnalysis: List<TaskPairDto>,
    val analyzedTasks: List<TaskPairDto>
)