package org.github.hrautoassignertaskhoursforecast.WorkStream.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.NaturalId

@Entity
@Table(name = "workstream")
data class WorkStream(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false, columnDefinition = "TEXT")
    val workstream: String = "",

    @Column(nullable = false, length = 255)
    val availableJobs: String = ""
)