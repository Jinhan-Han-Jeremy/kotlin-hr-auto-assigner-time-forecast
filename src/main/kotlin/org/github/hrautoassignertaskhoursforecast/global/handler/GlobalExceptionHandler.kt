package org.github.hrautoassignertaskhoursforecast.global.handler


import org.github.hrautoassignertaskhoursforecast.global.exception.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant
import org.slf4j.LoggerFactory

/**
 * 전역 예외 처리 핸들러 클래스
 * - 모든 컨트롤러에서 발생하는 예외를 일관된 방식으로 처리한다.
 * - HTTP 상태 코드, 오류 메시지, 발생 시각 등을 공통 포맷으로 응답한다.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * 공통적으로 사용될 에러 응답 DTO를 생성하는 함수
     *
     * @param status HTTP 상태 코드
     * @param message 오류 메시지
     * @return 에러 정보를 담은 ErrorResponse DTO를 ResponseEntity 형태로 반환
     */
    private fun errorResponse(status: HttpStatus, message: String): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = status.value(),
            message = message,
            timestamp = Instant.now().toEpochMilli()
        )
        return ResponseEntity(errorResponse, status)
    }

    /**
     * ResourceNotFoundException 처리
     * - 요청한 리소스를 찾을 수 없을 때 발생
     * - 404 NOT_FOUND 상태 코드 반환
     */
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        logger.warn("ResourceNotFoundException occurred: ${ex.message}")
        return errorResponse(HttpStatus.NOT_FOUND, ex.message ?: "Resource not found")
    }

    /**
     * RateLimitException 처리
     * - API 호출 횟수 제한 초과 시 발생
     * - 403 FORBIDDEN 상태 코드 반환
     */
    @ExceptionHandler(RateLimitException::class)
    fun handleRateLimitException(ex: RateLimitException): ResponseEntity<ErrorResponse> {
        logger.warn("RateLimitException occurred: ${ex.message}")
        return errorResponse(HttpStatus.FORBIDDEN, ex.message ?: "Rate limit exceeded")
    }

    /**
     * ForbiddenException 처리
     * - 접근 권한이 없는 경우 발생
     * - 403 FORBIDDEN 상태 코드 반환
     */
    @ExceptionHandler(ForbiddenException::class)
    fun handleForbiddenException(ex: ForbiddenException): ResponseEntity<ErrorResponse> {
        logger.warn("ForbiddenException occurred: ${ex.message}")
        return errorResponse(HttpStatus.FORBIDDEN, ex.message ?: "Access is forbidden")
    }

    /**
     * ServiceUnavailableException 처리
     * - 외부 서비스 불가 또는 내부 장애로 인해 서비스 이용 불가능할 때 발생
     * - 503 SERVICE_UNAVAILABLE 상태 코드 반환
     */
    @ExceptionHandler(ServiceUnavailableException::class)
    fun handleServiceUnavailableException(ex: ServiceUnavailableException): ResponseEntity<ErrorResponse> {
        logger.error("ServiceUnavailableException occurred: ${ex.message}")
        return errorResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.message ?: "Service unavailable")
    }

    /**
     * BadRequestException 처리
     * - 잘못된 요청(파라미터 오류, 형식 오류 등) 발생 시
     * - 400 BAD_REQUEST 상태 코드 반환
     */
    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(ex: BadRequestException): ResponseEntity<ErrorResponse> {
        logger.warn("BadRequestException occurred: ${ex.message}")
        return errorResponse(HttpStatus.BAD_REQUEST, ex.message ?: "Invalid request")
    }

    /**
     * 그 외 처리되지 않은 예외 처리
     * - 미처 정의되지 않은 예외 발생 시
     * - 500 INTERNAL_SERVER_ERROR 상태 코드 반환
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unhandled exception occurred: ${ex.message}", ex)
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred")
    }

}
