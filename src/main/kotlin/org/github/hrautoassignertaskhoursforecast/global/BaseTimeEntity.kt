package org.github.hrautoassignertaskhoursforecast.global

import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseTimeEntity {
//
//    var startedTime: LocalDateTime? = null
//
//    var endedTime: LocalDateTime? = null
//
//    // 작업 시작 시간을 수동으로 설정
//    fun setStartedTime(startedTime: LocalDateTime) {
//        this.startedTime = startedTime
//    }
//
//    // 작업 완수 시간을 수동으로 설정
//    fun setEndedTime(endedTime: LocalDateTime) {
//        this.endedTime = endedTime
//    }
}