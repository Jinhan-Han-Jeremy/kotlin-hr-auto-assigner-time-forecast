package org.github.hrautoassignertaskhoursforecast.analyzingCalculator.application.dto

import kotlin.math.min

data class analyzedResultsResponse (
    val resultsPriortizedByDifficulty : List<AssignedTeamsForTasksDto>,
    val expectedSpendingDaysInPriorityByDifficulty: Double = resultsPriortizedByDifficulty.map { it.duration }.minOrNull() ?: 0.0,
    val milpToAnalyzedResults : List<AssignedTeamsForTasksDto>,
    val expectedSpendingDaysInMilp: Double = milpToAnalyzedResults.map { it.duration }.minOrNull() ?: 0.0,
)