package org.github.hrautoassignertaskhoursforecast.Task.infrastructure.jdbc

import io.lettuce.core.dynamic.annotation.Param
import org.github.hrautoassignertaskhoursforecast.Task.domain.entity.Task

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TaskRepository : JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    fun findByTaskName(name: String): Task?

    @Query("SELECT t.id FROM Task t WHERE t.taskName IN :names")
    fun findIdsByTaskNames(@Param("names") names: List<String>): List<Long>

    @Query("SELECT t.taskName FROM Task t WHERE t.id IN :ids")
    fun findNamesByIds(@Param("ids") ids: List<Long>): List<String>

    fun findAllByDifficulty(difficulty: Int): List<Task>

}
