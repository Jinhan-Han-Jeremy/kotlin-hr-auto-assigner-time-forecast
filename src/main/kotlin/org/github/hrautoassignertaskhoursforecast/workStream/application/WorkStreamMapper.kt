package org.github.hrautoassignertaskhoursforecast.workStream.application

import org.github.hrautoassignertaskhoursforecast.workStream.application.dto.AnalyzedWorkStreamResponse
import org.github.hrautoassignertaskhoursforecast.workStream.application.dto.TaskPairDto
import org.github.hrautoassignertaskhoursforecast.workStream.application.dto.WorkStreamRequest
import org.github.hrautoassignertaskhoursforecast.workStream.application.dto.WorkStreamResponse
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