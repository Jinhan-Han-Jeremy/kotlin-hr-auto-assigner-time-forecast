package org.github.hrautoassignertaskhoursforecast.global.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

// Redis 연결 전략 설정을 위한 스프링 구성 클래스
@Configuration
class RedisConfig {
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        // RedisTemplate 생성
        val template = RedisTemplate<String, Any>()

        // Redis 연결 팩토리 설정
        template.setConnectionFactory(connectionFactory)

        // Key는 문자열로 직렬화
        template.keySerializer = StringRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()

        // Value는 JSON으로 직렬화
        val objectMapper = ObjectMapper().apply {
            registerModule(JavaTimeModule()) // Java 8 날짜/시간 API 지원
            findAndRegisterModules() // 기타 모듈 자동 등록
        }
        val jsonSerializer = GenericJackson2JsonRedisSerializer(objectMapper)
        template.valueSerializer = jsonSerializer
        template.hashValueSerializer = jsonSerializer

        // Template 설정 초기화
        template.afterPropertiesSet()

        return template
    }
}