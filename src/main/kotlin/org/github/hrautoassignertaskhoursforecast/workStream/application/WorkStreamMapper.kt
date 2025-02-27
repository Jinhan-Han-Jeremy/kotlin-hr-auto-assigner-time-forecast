package org.github.hrautoassignertaskhoursforecast.workStream.application

import org.github.hrautoassignertaskhoursforecast.workStream.application.dto.*
import org.github.hrautoassignertaskhoursforecast.workStream.domain.entity.WorkStream
import org.springframework.stereotype.Component

@Component
class WorkStreamMapper {
    fun toEntity(dto: WorkStreamRequest): WorkStream {
        return WorkStream(
            workstream = dto.workstream,
            availableJobs = dto.availableJobs
        )
    }

    fun toResponseDto(entity: WorkStream): WorkStreamResponse {
        return WorkStreamResponse(
            id = entity.id,
            workstream = entity.workstream,
            availableJobs = entity.availableJobs
        )
    }

    fun toResponseWithTasksDto(entity: WorkStream, tasksNames: List<String>): WorkStreamResponseWithTasks {
        return WorkStreamResponseWithTasks(
            id = entity.id,
            workstream = entity.workstream,
            assignedTasksNames = tasksNames,
            availableJobs = entity.availableJobs
        )
    }

    fun toResponseAnalysisDto(
        workstream: String,
        tasksByTrieAnalysis: List<TaskPairDto>,
        tasksByTextAnalysis: List<TaskPairDto>,
        analyzedTasks: List<TaskPairDto>
    ): AnalyzedWorkStreamResponse {
        return AnalyzedWorkStreamResponse(
            workstream = workstream,
            tasksByTrieAnalysis = tasksByTrieAnalysis,
            tasksByTextAnalysis = tasksByTextAnalysis,
            analyzedTasks = analyzedTasks
        )
    }
}