package org.github.hrautoassignertaskhoursforecast.TaskHistory.domain.vo

import org.github.hrautoassignertaskhoursforecast.Task.domain.entity.vo.EmployeeRoles
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = false)
class TeamMembersConverter : AttributeConverter<TeamMembersVO, String> {
    override fun convertToDatabaseColumn(attribute: TeamMembersVO?): String? {
        return attribute?.members?.joinToString(",")
    }

    override fun convertToEntityAttribute(dbData: String?): TeamMembersVO? {
        return dbData
            ?.takeIf { it.isNotBlank() }
            ?.split(",")
            ?.map { it.trim() }
            ?.let { TeamMembersVO(it) }
    }
}
