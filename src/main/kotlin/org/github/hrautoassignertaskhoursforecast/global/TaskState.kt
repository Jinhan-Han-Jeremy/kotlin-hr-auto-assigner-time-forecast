
package org.github.hrautoassignertaskhoursforecast.global

import com.fasterxml.jackson.annotation.JsonValue
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

enum class TaskState(val value: String) {
    NOT_STARTED("not started"),
    IN_PROGRESS("in progress"),
    DONE("done");

    @JsonValue
    override fun toString(): String {
        return value
    }

    companion object {
        fun from(status: String): TaskState {
            return values().find { it.value.equals(status, ignoreCase = true) }
                ?: throw IllegalArgumentException("Invalid status: $status")
        }
    }
}

@Converter(autoApply = true)
class TaskStateConverter : AttributeConverter<TaskState, String> {
    override fun convertToDatabaseColumn(attribute: TaskState?): String? {
        return attribute?.value
    }

    override fun convertToEntityAttribute(dbData: String?): TaskState? {
        if (dbData == null) return null
        return TaskState.from(dbData)
    }
}