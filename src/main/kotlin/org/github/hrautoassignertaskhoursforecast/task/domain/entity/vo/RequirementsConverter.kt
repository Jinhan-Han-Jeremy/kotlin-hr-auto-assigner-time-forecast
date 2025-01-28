package org.github.hrautoassignertaskhoursforecast.task.domain.entity.vo

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = false)
class RequirementsConverter : AttributeConverter<Requirements, String?> {

    override fun convertToDatabaseColumn(attribute: Requirements?): String? {
        // VO -> DB 저장용 문자열 (쉼표로 합쳐진 문자열)
        return attribute?.getRequirementContent()
    }

    override fun convertToEntityAttribute(dbData: String?): Requirements? {
        // DB에서 읽은 문자열 -> VO
        return dbData?.let {
            Requirements(it)
        }
    }
}