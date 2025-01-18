package org.github.hrautoassignertaskhoursforecast.global.handler
/**
 * 에러 응답을 위한 DTO 클래스
 * - 모든 예외 상황에서 공통적으로 반환할 정보 정의
 * @param status HTTP 상태 코드
 * @param message 오류 관련 메시지
 * @param timestamp 오류 발생 시각(밀리초)
 */
data class ErrorResponse(
    val status: Int,
    val message: String,
    val timestamp: Long
)