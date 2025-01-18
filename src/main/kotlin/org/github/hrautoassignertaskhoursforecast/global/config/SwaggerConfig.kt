package org.github.hrautoassignertaskhoursforecast.global.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(apiInfo())
        // 인증 스키마 및 보안 요구사항
        // .addSecurityItem(securityRequirement())
         .components(securityComponents())
    }

    /**
     * API 정보 설정
     */
    private fun apiInfo(): Info {
        return Info()
            .title("API Documentation")
            .version("1.0")
            .description("API documentation without authentication requirement")
    }


    // 보안설정을 완전히 제거했으므로 아래 메서드들은 더 이상 필요없음.
    // 필요하다면 주석처리하거나 삭제해도 무방.
    private fun securityRequirement(): SecurityRequirement {
        return SecurityRequirement().addList("bearerAuth")
    }

    private fun securityComponents(): Components {
        return Components()
            .addSecuritySchemes(
                "bearerAuth",
                SecurityScheme()
                    .name("bearerAuth")
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
            )
    }
}