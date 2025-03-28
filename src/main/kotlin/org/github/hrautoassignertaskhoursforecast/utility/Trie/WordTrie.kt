package org.github.hrautoassignertaskhoursforecast.utility.Trie

import org.springframework.stereotype.Component

@Component
class WordTrie {
    private val root = Node()

    /**
     * 한 문장을 "단어 배열"로 분할 후, 각 단어를 root.children[...]에 연결
     */
    @Synchronized
    fun insertTaskName(originalTaskName: String) {
        val words = tokenize(originalTaskName)
        for (word in words) {
            val node = root.children.computeIfAbsent(word) { Node() }
            node.isEndOfWord = true
            node.taskNames.add(originalTaskName)
        }
    }

    /**
     * 여러 TaskName을 한꺼번에 삽입
     */
    fun insertAll(taskNames: List<String>) {

        taskNames.forEach { insertTaskName(it) }
    }

    /**
     * 검색: 특정 단어 exact match 시, 그 노드의 taskNames를 반환
     */
    fun searchWord(word: String): List<String> {
        val w = normalize(word)
        val node = root.children[w] ?: return emptyList()
        return node.taskNames.toList()
    }

    /**
     * 여러 토큰을 입력받아, 각각 검색 -> 합집합 반환
     */
    fun searchByTokens(tokens: List<String>): Set<String> {
        val result = mutableSetOf<String>()
        for (i in tokens.indices) {
            for (j in i + 1..tokens.size) {
                val phrase = tokens.subList(i, j).joinToString(" ")
                result.addAll(searchWord(phrase))
            }
        }
        return result
    }
    fun tokenize(sentence: String): List<String> {
        // 간단히 공백 split + 정규화
        return sentence.split("\\s+".toRegex())
            .map { normalize(it) }
            .filter { it.isNotEmpty() }
    }

    private fun normalize(input: String): String {
        return input.lowercase().replace(Regex("[^a-z0-9가-힣]"), "")
    }
}