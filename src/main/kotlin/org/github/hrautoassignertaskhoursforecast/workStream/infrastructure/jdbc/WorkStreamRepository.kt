package org.github.hrautoassignertaskhoursforecast.workStream.infrastructure.jdbc
import org.github.hrautoassignertaskhoursforecast.workStream.domain.entity.WorkStream
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WorkStreamRepository : JpaRepository<WorkStream, Int>