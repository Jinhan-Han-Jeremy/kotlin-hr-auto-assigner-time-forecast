package org.github.hrautoassignertaskhoursforecast.utility.Trie

import org.github.hrautoassignertaskhoursforecast.global.exception.ResourceNotFoundException
import org.github.hrautoassignertaskhoursforecast.utility.SimilarityCalculator
import org.springframework.stereotype.Component

//Trie를 운용하는 클래스들
@Component
class TrieRecommender(private val wordTrie: WordTrie) {

    //입력 정보 토큰화
    private fun getTokensFromInfo(workInfo: String, tasksNames: List<String>): List<String>
    {
        wordTrie.insertAll(tasksNames)

        // 입력 텍스트 토큰화
        val tokens = wordTrie.tokenize(workInfo)
        if (tokens.isEmpty()) {
            throw ResourceNotFoundException("입력 데이터가 유효하지 않습니다. 유효한 텍스트를 입력하세요.")
        }

        return tokens
    }

    private fun targetsPairedByCalculated(candidateSet: Set<String>, tasksNames: List<String>, tokens: List<String>):
            List<Pair<String, Double>> {

        //taskNames로 대체
        val targetList = when {
            candidateSet.isNotEmpty() -> candidateSet
            else -> tasksNames
        }

        return targetList.map { candidate ->
            val score = SimilarityCalculator.similarity(tokens.joinToString(" "), candidate)
            candidate to score
        }
    }


    /** Trie를 만들고, workInfo 토큰화 -> 후보 검색 -> 유사도 계산 -> 상위 5개 추천
     */
    fun recommendTasksByTrie(workInfo: String, tasksNames: List<String>): List<String> {

        // 입력 텍스트 토큰화
        val tokens = getTokensFromInfo(workInfo, tasksNames)

        // Trie 기반으로 후보 검색
        val candidateSet = wordTrie.searchByTokens(tokens)
        if (candidateSet.isEmpty()) {
            println("Trie에서 후보 작업을 찾지 못했습니다. 전체 태스크를 사용합니다.")
        }

        // 유사도 계산
        val targetPairs = targetsPairedByCalculated(candidateSet, tasksNames, tokens)

        // 점수가 0.1 이상인 상위 5개 선별
        val recommended = targetPairs
            .filter { it.second >= 0.1 }
            .sortedByDescending { it.second }
            .take(5)
            .map { it.first }

        println("Trie 기반 추천 작업: $recommended")
        return recommended
    }

}