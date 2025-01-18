package org.github.hrautoassignertaskhoursforecast.utility

object SimilarityCalculator {


    // 문자열 유사도 계산
    fun similarity(input: String, candidate: String): Double {
        if (input.isBlank() || candidate.isBlank()) return 0.0

        // 레벤슈타인 유사도
        val levenshteinScore = calculateLevenshteinSimilarity(input, candidate)

        // 두 점수의 가중 평균
        return levenshteinScore
    }

    // 레벤슈타인 기반 유사도 계산
    private fun calculateLevenshteinSimilarity(s1: String, s2: String): Double {
        val maxLen = maxOf(s1.length, s2.length)
        if (maxLen == 0) return 1.0
        val dist = levenshteinDistance(s1, s2)
        return 1.0 - dist.toDouble() / maxLen
    }

    // 레벤슈타인 거리 계산
    fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) {
            for (j in 0..s2.length) {
                dp[i][j] = when {
                    i == 0 -> j
                    j == 0 -> i
                    else -> {
                        val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                        minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
                    }
                }
            }
        }
        return dp[s1.length][s2.length]
    }
}