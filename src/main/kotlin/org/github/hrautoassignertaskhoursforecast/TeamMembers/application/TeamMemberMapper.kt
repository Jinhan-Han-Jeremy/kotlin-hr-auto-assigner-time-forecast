package org.github.hrautoassignertaskhoursforecast.TeamMembers.application

import org.github.hrautoassignertaskhoursforecast.TeamMembers.application.dto.TeamMemberResponseDTO
import org.github.hrautoassignertaskhoursforecast.TeamMembers.domain.entity.TeamMember
import org.github.hrautoassignertaskhoursforecast.TeamMembers.domain.entity.vo.PerformanceForTask
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TeamMemberMapper {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun toResponseDto(entity: TeamMember): TeamMemberResponseDTO {
        //로그 추적용
        logger.debug("Mapping TeamMember to DTO:")
        logger.debug("ID: ${entity.id}")
        logger.debug("Name from DB: ${entity.teamMemberName}")
        logger.debug("Role from DB: ${entity.teamMemberRole}")
        logger.debug("Performances from DB: ${entity.performancesForTasks.getAllPerformancesForTasks()}")

        val dto = TeamMemberResponseDTO(
            id = entity.id ?: throw IllegalStateException("Member ID cannot be null"),
            teamMemberName = entity.teamMemberName,
            teamMemberRole = entity.teamMemberRole,
            level = entity.level,
            isWorking = entity.isWorking,
            performancesForTasks = entity.performancesForTasks.getAllPerformancesForTasks(),
            achievementsScore = entity.achievementsScore
        )

        logger.debug("Created DTO:")
        logger.debug("DTO Name: ${dto.teamMemberName}")
        logger.debug("DTO Role: ${dto.teamMemberRole}")
        logger.debug("DTO Performances: ${dto.performancesForTasks}")

        return dto
    }

    fun toEntity(dto: TeamMemberResponseDTO): TeamMember {
        // 데이터 검증 예시
        require(dto.level >= 0) { "Level must be non-negative" }
        return TeamMember(
            id = dto.id,
            teamMemberName = dto.teamMemberName,
            teamMemberRole = dto.teamMemberRole,
            level = dto.level,
            isWorking = dto.isWorking,
            performancesForTasks = PerformanceForTask.fromMap(dto.performancesForTasks),
            achievementsScore = dto.achievementsScore
        )
    }
}