package org.github.hrautoassignertaskhoursforecast.teamMembers.domain.entity.vo

import jakarta.persistence.Column
import jakarta.persistence.Convert
import org.slf4j.Logger
import org.slf4j.LoggerFactory

data class PerformanceForTask(
    @Column(name = "performance_for_skills", columnDefinition = "json")
    @Convert(converter = PerformanceConverter::class)
    private val performancesForTasks: MutableMap<String, Int> = mutableMapOf()
) {

    fun getScoreByTask(type: String): Int? = performancesForTasks[type]

    fun getAllPerformancesForTasks(): Map<String, Int> = performancesForTasks.toMap()

    fun setPerformanceForTask(type: String, score: Int) {
        require(score in VALID_SCORE_RANGE) { "Score must be between ${VALID_SCORE_RANGE.first} and ${VALID_SCORE_RANGE.last}." }
        performancesForTasks[type] = score
    }

    fun setPerformanceNameForTask(type: String, newTypeName: String) {
        val value = checkNotNull(performancesForTasks[type]) {
            "Task type '$type' not found"
        }
        performancesForTasks -= type
        performancesForTasks[newTypeName] = value
    }

    fun updatePerformancesForTasks(newPerformances: Map<String, Int>, shouldAdd: Boolean) {
        require(newPerformances.values.all { it in VALID_SCORE_RANGE }) {
            "All scores must be between ${VALID_SCORE_RANGE.first} and ${VALID_SCORE_RANGE.last}."
        }

        when {
            !shouldAdd -> {
                performancesForTasks.clear()
                performancesForTasks.putAll(newPerformances)
            }
            !hasOverlappingKeys(newPerformances) -> {
                val matchingTasks = getMatchingTasks(newPerformances)
                logger.debug("Excluding matching tasks and adding new ones: $matchingTasks")
                performancesForTasks.putAll(
                    newPerformances.filterKeys { it !in performancesForTasks.keys }
                )
            }
        }
    }

    private fun hasOverlappingKeys(other: Map<String, Int>): Boolean =
        other.keys.any { it in performancesForTasks.keys }

    private fun getMatchingTasks(newPerformances: Map<String, Int>): Map<String, Int> =
        newPerformances.filterKeys { it in performancesForTasks.keys }

    companion object {
        private val VALID_SCORE_RANGE = 0..100
        private val logger: Logger = LoggerFactory.getLogger(PerformanceForTask::class.java)

        fun fromMap(performances: Map<String, Int>): PerformanceForTask =
            PerformanceForTask(performances.toMutableMap())
    }
}
