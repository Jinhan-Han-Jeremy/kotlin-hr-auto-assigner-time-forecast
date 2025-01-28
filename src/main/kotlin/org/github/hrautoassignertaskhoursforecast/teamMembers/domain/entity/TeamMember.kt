package org.github.hrautoassignertaskhoursforecast.teamMembers.domain.entity

import jakarta.persistence.*
import org.github.hrautoassignertaskhoursforecast.teamMembers.domain.entity.vo.PerformanceConverter
import org.github.hrautoassignertaskhoursforecast.teamMembers.domain.entity.vo.PerformanceForTask

@Entity
@Table(name = "team_member")
data class TeamMember(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "name")
    val teamMemberName: String,

    @Column(name = "role")
    var teamMemberRole: String,

    @Column(name = "level")
    var level: Int,

    @Column(name = "state")
    var isWorking: Boolean,

    @Column(name = "performance_for_skills", columnDefinition = "json")
    @Convert(converter = PerformanceConverter::class)
    var performancesForTasks: PerformanceForTask,

    @Column(name = "achievements_score")
    var achievementsScore: Float
){
}