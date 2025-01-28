package org.github.hrautoassignertaskhoursforecast.taskHistory.domain.vo

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = false)
class TeamMembersConverter : AttributeConverter<TeamMembersVO, String> {
    override fun convertToDatabaseColumn(attribute: TeamMembersVO?): String? {
        return attribute?.membersNames?.joinToString(",")
    }

    // DB에 사용한데이터를 표현
    override fun convertToEntityAttribute(dbData: String?): TeamMembersVO? {
        return dbData
            ?.takeIf { it.isNotBlank() }
            ?.split(",")
            ?.map { it.trim() }
            ?.let { TeamMembersVO(it) }
    }
}
