
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
            val lower = status.lowercase()
            return when {
                lower.startsWith("not_started") || lower.startsWith("not started") -> NOT_STARTED
                lower.startsWith("in_progress") || lower.startsWith("in progress") -> IN_PROGRESS
                lower.startsWith("done") -> DONE
                else -> throw IllegalArgumentException("Invalid status: $status")
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
        print("dbData wht is :$dbData")

        val lower = dbData.lowercase()
        // 만약 lower가 "done"으로 시작하면 => DONE
        if (lower.startsWith("done")) {
            return TaskState.DONE
        } else if (lower.contains("not started")) {
            return TaskState.NOT_STARTED
        } else if (lower.contains("in progress")) {
            return TaskState.IN_PROGRESS
        }
        else
            return null
    }
}
}