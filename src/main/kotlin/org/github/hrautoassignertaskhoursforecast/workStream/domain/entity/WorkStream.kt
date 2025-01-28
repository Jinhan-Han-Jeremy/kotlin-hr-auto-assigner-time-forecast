package org.github.hrautoassignertaskhoursforecast.workStream.domain.entity

import jakarta.persistence.*

@Entity
@Table(name = "workstream_info")
data class WorkStream(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false, columnDefinition = "TEXT")
    val workstream: String = "",

    @Column(nullable = false, length = 255)
    val availableJobs: String = ""
)