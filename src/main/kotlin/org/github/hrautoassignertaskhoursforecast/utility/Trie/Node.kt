package org.github.hrautoassignertaskhoursforecast.utility.Trie

internal class Node {
    val children: MutableMap<String, Node> = mutableMapOf()
    val taskNames: MutableSet<String> = mutableSetOf()
    var isEndOfWord: Boolean = false
}