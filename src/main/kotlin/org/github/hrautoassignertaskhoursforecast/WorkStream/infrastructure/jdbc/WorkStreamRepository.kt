package org.github.hrautoassignertaskhoursforecast.WorkStream.infrastructure.jdbc
import org.github.hrautoassignertaskhoursforecast.WorkStream.domain.entity.WorkStream
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WorkStreamRepository : JpaRepository<WorkStream, Int>