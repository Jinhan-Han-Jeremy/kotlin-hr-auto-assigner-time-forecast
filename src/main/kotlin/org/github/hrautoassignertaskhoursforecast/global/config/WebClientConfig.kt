package org.github.hrautoassignertaskhoursforecast.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {
    @Bean
    fun fastApiWebClient(builder: WebClient.Builder): WebClient {
        return builder
            .baseUrl("http://hr-auto-assigner-fastapi:8000")  // Docker Compose에 의해서 통신 가능한 호스트 이름
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            // 요청 헤더에 Content_Type을 JSON으로 지정하여 FastAPI 서버가 JSON 데이터를 받을 수 있도록 설정.
            .build()
    }
}