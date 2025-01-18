package org.github.hrautoassignertaskhoursforecast.TeamMembers.infrastructure.jdbc

import org.github.hrautoassignertaskhoursforecast.TeamMembers.domain.entity.TeamMember
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface TeamMemberRepository : JpaRepository<TeamMember, Long>, JpaSpecificationExecutor<TeamMember> {
    fun findAllByIsWorking(isWorking: Boolean): List<TeamMember>
    fun findByTeamMemberNameContaining(name: String): TeamMember
    fun findByTeamMemberName(name: String): TeamMember
    fun findByPerformancesForTasksContaining(type: String): List<TeamMember>
}