package org.github.hrautoassignertaskhoursforecast.task.domain.entity.vo

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = false)
class EmployeeRolesConverter : AttributeConverter<EmployeeRoles, String> {
    override fun convertToDatabaseColumn(attribute: EmployeeRoles?): String {
        return attribute?.getRolesStr() ?: ""
    }

    override fun convertToEntityAttribute(dbData: String?): EmployeeRoles {
        return EmployeeRoles(dbData ?: "")
    }
}