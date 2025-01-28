package org.github.hrautoassignertaskhoursforecast.teamMembers.domain.entity.vo

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class PerformanceConverter : AttributeConverter<PerformanceForTask, String> {
    companion object {
        private val objectMapper = ObjectMapper()
    }

    override fun convertToDatabaseColumn(attribute: PerformanceForTask?): String =
        objectMapper.writeValueAsString(attribute?.getAllPerformancesForTasks() ?: emptyMap<String, Int>())

    override fun convertToEntityAttribute(dbData: String?): PerformanceForTask {
        if (dbData.isNullOrEmpty()) return PerformanceForTask()
        return try {
            val map: Map<String, Int> = objectMapper.readValue(dbData,
                object : TypeReference<Map<String, Int>>() {})
            PerformanceForTask.fromMap(map)
        } catch (e: Exception) {
            PerformanceForTask()
        }
    }
}