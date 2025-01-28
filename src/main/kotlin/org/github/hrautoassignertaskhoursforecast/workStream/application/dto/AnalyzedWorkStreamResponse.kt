package org.github.hrautoassignertaskhoursforecast.workStream.application.dto

data class AnalyzedWorkStreamResponse(
    val workstream: String,
    val tasksByTrieAnalysis: List<TaskPairDto>,
    val tasksByTextAnalysis: List<TaskPairDto>,
    val analyzedTasks: List<TaskPairDto>
)