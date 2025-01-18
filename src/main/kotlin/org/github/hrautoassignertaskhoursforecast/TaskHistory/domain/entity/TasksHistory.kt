package org.github.hrautoassignertaskhoursforecast.TaskHistory.domain.entity

import org.github.hrautoassignertaskhoursforecast.Task.domain.entity.vo.EmployeeRolesConverter
import org.github.hrautoassignertaskhoursforecast.TaskHistory.domain.vo.TeamMembersConverter
import org.github.hrautoassignertaskhoursforecast.TaskHistory.domain.vo.TeamMembersVO
import jakarta.persistence.*
import org.github.hrautoassignertaskhoursforecast.Task.domain.entity.vo.EmployeeRoles
import org.github.hrautoassignertaskhoursforecast.global.TaskState
import org.github.hrautoassignertaskhoursforecast.global.TaskStateConverter
import java.time.LocalDate

@Entity
@Table(name = "tasks_history")
data class TasksHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long= 0L, // 기본값 설정으로 null 문제 해결

    @Column(nullable = false)
    val name: String,

    @Convert(converter = TeamMembersConverter::class)
    @Column(name = "teammembers", length = 255)
    val teamMembers: TeamMembersVO? = null,

    @Convert(converter = EmployeeRolesConverter::class)
    @Column(name = "available_jobs")
    var employeeRoles: EmployeeRoles = EmployeeRoles(""),

    @Column(name = "spending_days")
    val spendingDays: Float? = null,

    @Column(name = "expected_days")
    val expectedDays: Float? = null,

    @Convert(converter = TaskStateConverter::class)
    @Column(name = "state")
    var state: TaskState = TaskState.NOT_STARTED,

    @Column(name = "requirements_satisfied")
    var requirementsSatisfied: Boolean? = false,

    // ▼ MySQL 8.0이므로 DATETIME(6) 지정 가능
    @Column(name = "started_at", columnDefinition = "DATETIME(6)")
    val startedAt: LocalDate? = null,

    @Column(name = "ended_at", columnDefinition = "DATETIME(6)")
    val endedAt: LocalDate? = null
)