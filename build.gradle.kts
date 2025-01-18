plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.1.0"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("plugin.jpa") version "1.9.25"
}

group = "org.github"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21)) // `set`으로 Property 설정
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {

//    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
// Spring Data JDBC를 사용하여 데이터베이스와 간단하게 상호작용할 수 있는 기본 지원 제공

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
// Spring Data JPA를 사용하여 데이터베이스와의 상호작용을 쉽게 처리 (ORM 지원)

    implementation("org.apache.commons:commons-text:1.10.0")

    implementation("org.springframework.boot:spring-boot-starter-web")
// RESTful API 및 웹 애플리케이션 개발을 위한 Spring MVC와 Tomcat 지원

    // coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.9.0")
// 비동기, 반응형 웹 애플리케이션 개발을 위한 Spring WebFlux 지원
    implementation("org.springframework.boot:spring-boot-starter-webflux:3.1.0")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
// Kotlin 객체를 JSON으로 직렬화/역직렬화하기 위한 Jackson 모듈

    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
// Reactor를 위한 Kotlin 확장 기능 제공 (반응형 프로그래밍 지원)

    implementation("org.jetbrains.kotlin:kotlin-reflect")
// Kotlin 리플렉션을 사용하여 런타임에 클래스, 메서드, 속성 등 정보 탐색 가능

//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
// Reactor와 Kotlin Coroutines 간의 통합을 지원 (반응형 코틀린 프로그래밍)

    compileOnly("org.projectlombok:lombok")
// Lombok은 getter, setter, toString, equals 등 반복적인 코드를 줄이기 위해 컴파일 타임에서만 사용하는 도구

    developmentOnly("org.springframework.boot:spring-boot-devtools")
// 개발 중에 자동으로 애플리케이션을 다시 로드하거나 캐싱을 비활성화해주는 도구

    runtimeOnly("com.mysql:mysql-connector-j")
// MySQL 데이터베이스와 연결하기 위한 JDBC 드라이버

    annotationProcessor("org.projectlombok:lombok")
// Lombok의 애노테이션을 처리하여 컴파일 타임에 코드 생성

    implementation("org.springframework.boot:spring-boot-starter-security")
// Spring Security를 통해 인증 및 권한 부여 기능 제공

    implementation("org.springframework.boot:spring-boot-starter-data-redis")
// Redis 데이터베이스와의 통합 및 캐싱, 데이터 저장소 지원

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
// OpenAPI(Swagger)를 사용해 API 문서화 및 Swagger UI 제공

    testImplementation("org.springframework.boot:spring-boot-starter-test")
// Spring Boot의 테스트 기능 제공 (JUnit 및 Mock 지원)

    testImplementation("io.projectreactor:reactor-test")
// Reactor 기반 애플리케이션 테스트를 위한 라이브러리

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
// JUnit5 기반의 Kotlin 테스트 지원

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
// JUnit 플랫폼 런처로 테스트 실행을 위한 런타임 의존성

}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
