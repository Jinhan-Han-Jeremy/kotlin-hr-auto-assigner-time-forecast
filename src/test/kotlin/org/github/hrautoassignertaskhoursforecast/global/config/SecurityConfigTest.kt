package org.github.hrautoassignertaskhoursforecast.global.config

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.web.FilterChainProxy

/**
 * [수정 요점]
 * - @SpringBootTest 단독 사용 + @EnableAutoConfiguration(exclude=...)로 DB AutoConfig 제외
 * - DB 관련 오류 (UnknownHostException 등) 방지
 */
@SpringBootTest
@EnableAutoConfiguration(
    exclude = [
        DataSourceAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class
    ]
)
class SecurityConfigTest {

    @Autowired
    private lateinit var securityFilterChain: FilterChainProxy


}