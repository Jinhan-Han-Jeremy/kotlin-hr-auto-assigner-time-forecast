package org.github.hrautoassignertaskhoursforecast.WorkStream.application

import org.github.hrautoassignertaskhoursforecast.WorkStream.application.dto.AnalyzedWorkStreamResponse
import org.github.hrautoassignertaskhoursforecast.WorkStream.application.dto.TaskPairDto
import org.github.hrautoassignertaskhoursforecast.WorkStream.application.dto.WorkStreamRequest
import org.github.hrautoassignertaskhoursforecast.WorkStream.application.dto.WorkStreamResponse
import org.github.hrautoassignertaskhoursforecast.WorkStream.domain.entity.WorkStream
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

    fun toResponseAnalysisDto(
        workstream: String,
        tasksByTrieAnalysis: List<TaskPairDto>,
        tasksByTextAnalysis: List<TaskPairDto>,
        analyzedTasks: List<TaskPairDto>
    ): AnalyzedWorkStreamResponse {
        return AnalyzedWorkStreamResponse(
            workstream = workstream,
            tasksByTrieAnalysis = tasksByTrieAnalysis,
            tasksdByTextAnalysis = tasksByTextAnalysis,
            analyzedTasks = analyzedTasks
        )
    }
}