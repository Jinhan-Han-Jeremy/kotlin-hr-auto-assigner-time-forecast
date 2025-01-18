package org.github.hrautoassignertaskhoursforecast.global.config

import org.github.hrautoassignertaskhoursforecast.utility.Trie.WordTrie
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope

@Configuration
class RequiredConfig {
    @Bean
    @Scope("prototype") // 요청마다 새로운 인스턴스 생성
    fun wordTrie(): WordTrie {
        return WordTrie()
    }
}