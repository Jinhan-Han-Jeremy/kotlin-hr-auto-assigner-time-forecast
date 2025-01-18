package org.github.hrautoassignertaskhoursforecast.Task.application.service
import org.github.hrautoassignertaskhoursforecast.Task.application.TaskMapper
import org.github.hrautoassignertaskhoursforecast.Task.application.dto.TaskRequestDTO
import org.github.hrautoassignertaskhoursforecast.Task.application.dto.TaskResponseDTO
import org.github.hrautoassignertaskhoursforecast.Task.infrastructure.jdbc.TaskRepository
import org.github.hrautoassignertaskhoursforecast.global.RoleType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.github.hrautoassignertaskhoursforecast.Task.application.dto.TaskSearchDTO
import org.github.hrautoassignertaskhoursforecast.Task.domain.entity.Task
import org.github.hrautoassignertaskhoursforecast.global.exception.ResourceNotFoundException
import org.springframework.data.jpa.domain.Specification

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val taskMapper: TaskMapper
) {

    /**
     * 모든 Task 조회 */
    @Transactional(readOnly = true)
    suspend fun findAllTasks(): List<TaskResponseDTO> =
        withContext(Dispatchers.IO) {

        taskRepository.findAll().map { taskMapper.toResponseDto(it) }
    }

    /**
     * ID로 Task 조회 */
    @Transactional(readOnly = true)
    suspend fun findTaskById(id: Long): TaskResponseDTO =
        withContext(Dispatchers.IO) {

        taskRepository.findById(id)
            .map { taskMapper.toResponseDto(it) }
            .orElseThrow { NoSuchElementException("Task with ID $id not found") }
    }

    /**
     * 새로운 작업 생성 */
    @Transactional
    suspend fun createTask(requestDTO: TaskRequestDTO): TaskResponseDTO =
        withContext(Dispatchers.IO) {

        val newTaskEntity = taskMapper.toEntity(requestDTO)
        val savedTask = taskRepository.save(newTaskEntity)
        taskMapper.toResponseDto(savedTask)
    }
    /**
     * 이름으로 Task 조회 */
    suspend fun findTaskByName(name: String): TaskResponseDTO =
        withContext(Dispatchers.IO) {

        val task = taskRepository.findByTaskName(name)
            ?: throw ResourceNotFoundException("Task with name $name not found")
        taskMapper.toResponseDto(task)
    }

    /**
     * 이름으로 Task id 조회 */
    suspend fun findIdsByTaskNames(names: List<String>): List<Long> =
        withContext(Dispatchers.IO) {

        val ids = taskRepository.findIdsByTaskNames(names)

        // 이름으로 ID를 찾은 결과와 요청한 이름의 차집합 계산
        val foundNames = taskRepository.findNamesByIds(ids) // ID를 통해 실제 이름 목록 조회
        val missingNames = names - foundNames  // 요청한 이름에서 찾은 이름을 뺀 차집합

        // 예외 처리
        if (missingNames.isNotEmpty()) {
            throw ResourceNotFoundException("Tasks not found for names: ${missingNames.joinToString(", ")}")
        }

        ids
    }

    /**
     * 작업 업데이트 */
    @Transactional
    suspend fun updateTask(id: Long, requestDTO: TaskRequestDTO): TaskResponseDTO =
        withContext(Dispatchers.IO) {

        val existingTask = taskRepository.findById(id)
            .orElseThrow { NoSuchElementException("Task with ID $id not found") }

        existingTask.updateTask(
            name = requestDTO.taskName,
            difficulty = requestDTO.difficulty,
            employeeRoles = requestDTO.toRoleTypes(),
            requirementsList = requestDTO.requirements
        )

        val savedTask = taskRepository.save(existingTask)
        taskMapper.toResponseDto(savedTask)
    }

    /**
     * 작업 삭제 */
    @Transactional
    suspend fun deleteTask(id: Long) =
        withContext(Dispatchers.IO) {

        val task = taskRepository.findById(id)
            .orElseThrow { NoSuchElementException("Task with ID $id not found") }
        taskRepository.delete(task)
    }

    /**
     * 특정 직무(Role)에 해당하는 Task들 찾기 */
    @Transactional(readOnly = true)
    suspend fun findTasksByRole(roleType: RoleType): List<TaskResponseDTO> =
        withContext(Dispatchers.IO) {

        taskRepository.findAll()
            .filter { it.employeeRoles.roles.contains(roleType) }
            .map { taskMapper.toResponseDto(it) }
    }

    @Transactional(readOnly = true)
    suspend fun searchTasks(criteria: TaskSearchDTO): List<TaskResponseDTO> = withContext(Dispatchers.IO) {
        var spec: Specification<Task> = Specification.where(null)

        // (1) taskName 조건
        criteria.taskName
            ?.takeIf { it.isNotBlank() }
            ?.let { taskName ->
                spec = spec.and(
                    Specification<Task> { root, _, cb ->
                        cb.like(
                            cb.lower(root.get("taskName")),
                            "%${taskName.lowercase()}%"
                        )
                    }
                )
            }

        // (2) difficulty 조건
        criteria.difficulty?.let { difficulty ->
            spec = spec.and(
                Specification<Task> { root, _, cb ->
                    cb.equal(root.get<Int>("difficulty"), difficulty)
                }
            )
        }

        // (3) employeeRoles 조건
        criteria.employeeRoles
            ?.takeIf { it.isNotEmpty() }
            ?.let { roles ->
                spec = spec.and(
                    Specification<Task> { root, _, cb ->
                        // 커스텀 VO이지만, DB에는 단일 컬럼 employee_role 로 저장됨
                        // JPA 입장에선 'employeeRoles'라는 하나의 필드(=문자열)로 접근 가능
                        val path = root.get<String>("employeeRoles")

                        // OR 조건 (roles 중 하나라도 포함되면 매칭)
                        val orPredicates = roles.map { role ->
                            cb.like(
                                cb.lower(path),
                                "%${role.lowercase()}%"
                            )
                        }
                        cb.or(*orPredicates.toTypedArray())
                    }
                )
            }

        // (4) requirements 조건
        criteria.requirements
            ?.takeIf { it.isNotEmpty() }
            ?.let { reqList ->
                spec = spec.and(
                    Specification<Task> { root, _, cb ->
                        // 커스텀 VO이지만, DB에는 단일 컬럼 requirements 로 저장
                        val path = root.get<String>("requirements")

                        val orPredicates = reqList.map { requirement ->
                            cb.like(
                                cb.lower(path),
                                "%${requirement.lowercase()}%"
                            )
                        }
                        cb.or(*orPredicates.toTypedArray())
                    }
                )
            }

        // 디버깅용
        println("최종 Specification: $spec")

        // 조건에 맞는 Task 엔티티 목록 조회
        val tasks = taskRepository.findAll(spec)

        // Task -> TaskResponseDTO 변환 후 반환
        return@withContext tasks.map { taskMapper.toResponseDto(it) }
    }

    /**
     * 특정 난이도 작업 조회 */
    @Transactional(readOnly = true)
    suspend fun findTasksByDifficulty(difficulty: Int): List<TaskResponseDTO> =
        withContext(Dispatchers.IO) {

        if (difficulty < 0) {
            throw IllegalArgumentException("Difficulty must be non-negative")
        }

        val tasks = taskRepository.findAllByDifficulty(difficulty)
        tasks.map { taskMapper.toResponseDto(it) }
    }
}
