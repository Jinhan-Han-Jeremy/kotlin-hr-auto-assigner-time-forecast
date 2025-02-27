package org.github.hrautoassignertaskhoursforecast.task.application.service
import org.github.hrautoassignertaskhoursforecast.task.application.TaskMapper
import org.github.hrautoassignertaskhoursforecast.task.application.dto.TaskRequestDTO
import org.github.hrautoassignertaskhoursforecast.task.application.dto.TaskResponseDTO
import org.github.hrautoassignertaskhoursforecast.task.infrastructure.jdbc.TaskRepository
import org.github.hrautoassignertaskhoursforecast.global.RoleType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.github.hrautoassignertaskhoursforecast.task.application.dto.TaskSearchDTO
import org.github.hrautoassignertaskhoursforecast.task.domain.entity.Task
import org.github.hrautoassignertaskhoursforecast.global.exception.ResourceNotFoundException
import org.github.hrautoassignertaskhoursforecast.taskHistory.infrastructure.jdbc.TasksHistoryRepository
import org.springframework.data.jpa.domain.Specification

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val taskMapper: TaskMapper,
    private val tasksHistoryRepository: TasksHistoryRepository,
) {

    /**
     * 모든 Task 조회 */
    @Transactional(readOnly = true)
    suspend fun findAllTasks(): List<TaskResponseDTO> {
        return taskRepository.findAll().map { taskMapper.toResponseDto(it) }
    }

    /**
     * 새로운 작업 생성 */
    @Transactional
    suspend fun createTask(requestDTO: TaskRequestDTO): TaskResponseDTO {
        val newTaskEntity = taskMapper.toEntity(requestDTO)
        val savedTask = taskRepository.save(newTaskEntity)
        return taskMapper.toResponseDto(savedTask)
    }

    /**
     * 작업 업데이트 */
    @Transactional
    suspend fun updateTask(id: Long, requestDTO: TaskRequestDTO): TaskResponseDTO {
        val existingTask = taskRepository.findById(id)

        existingTask.updateTask(
            name = requestDTO.taskName,
            difficulty = requestDTO.difficulty,
            employeeRoles = requestDTO.toRoleTypes(),
            requirementsList = requestDTO.requirements
        )

        val savedTask = taskRepository.save(existingTask)
        return taskMapper.toResponseDto(savedTask)
    }

    /**
     * 작업 삭제 */
    @Transactional
    suspend fun deleteTask(id: Long){
            val task = taskRepository.findById(id)
            taskRepository.delete(task)
        }

    /**
     * ID로 Task 조회 */
    @Transactional(readOnly = true)
    suspend fun findTaskById(id: Long): TaskResponseDTO {
        val task = taskRepository.findById(id)  // Task 객체 가져오기
        return taskMapper.toResponseDto(task)   // DTO 변환 후 반환
    }

    /**
     * 이름으로 Task 조회 */
    suspend fun findTaskByName(name: String): TaskResponseDTO {

        val task = taskRepository.findByTaskName(name)
            ?: throw ResourceNotFoundException("Task with name $name not found")

        return taskMapper.toResponseDto(task)
    }

    //difficulty를 기준으로 작업이름을 배열
    suspend fun targetedTaskNamesInTasksHistory(
        selectedTaskNames: List<String>
    ): List<String> {
        // 실제 존재하는 Task만 골라서 selectedTasks 리스트로 수집
        val selectedTasks = mutableListOf<TaskResponseDTO>()

        selectedTaskNames.forEach {
                selectedTaskName ->
            // tasksHistory에 존재하는지 확인 (없으면 예외 or 건너뛰기)
            val existingTasksHistory = tasksHistoryRepository.findByName(selectedTaskName)

            if (existingTasksHistory == null) {
                println("TasksHistory '$selectedTaskName' not found in DB") // 로그 추가
                throw ResourceNotFoundException("TasksHistory with name '$selectedTaskName' not found")
            }

            //존재하는 작업을 task로 추출
            val taskResponse = taskRepository.findByTaskName(existingTasksHistory.name)
                ?.let { taskMapper.toResponseDto(it) }
                ?: throw ResourceNotFoundException("Tasks with name '${existingTasksHistory.name}' not found")
            selectedTasks.add(taskResponse)
        }

        // difficulty 기준으로 내림차순 정렬 후
        val sortedTaskNames: List<String> = selectedTasks
            .sortedByDescending { it.difficulty } // difficulty 기준 내림차순
            .map { it.taskName }                  // 정렬된 목록에서 taskName 만 추출

        return sortedTaskNames
    }

    /**
     * 이름들로 Task ids 조회 */
    suspend fun findIdsByTaskNames(names: List<String>): List<Long> {

        val ids = taskRepository.findIdsByTaskNames(names)

        // 이름으로 ID를 찾은 결과와 요청한 이름의 차집합 계산
        val foundNames = taskRepository.findNamesByIds(ids) // ID를 통해 실제 이름 목록 조회
        val missingNames = names - foundNames  // 요청한 이름에서 찾은 이름을 뺀 차집합

        // 예외 처리
        if (missingNames.isNotEmpty()) {
            throw ResourceNotFoundException("Tasks not found for names: ${missingNames.joinToString(", ")}")
        }

        return ids
    }

    /**
     * 특정 직무(Role)에 해당하는 Task들 찾기 */
    @Transactional(readOnly = true)
    suspend fun findTasksByRole(roleType: RoleType): List<TaskResponseDTO>{

        return taskRepository.findAll()
            .filter { it.employeeRoles.roles.contains(roleType) }
            .map { taskMapper.toResponseDto(it) }
    }

    @Transactional(readOnly = true)
    suspend fun searchTasksByInput(criteria: TaskSearchDTO): List<TaskResponseDTO>{
        var spec: Specification<Task> = Specification.where(null)

        //taskName 조건
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

        // difficulty 조건
        criteria.difficulty?.let { difficulty ->
            spec = spec.and(
                Specification<Task> { root, _, cb ->
                    cb.equal(root.get<Int>("difficulty"), difficulty)
                }
            )
        }

        // employeeRoles 조건
        criteria.employeeRoles?.takeIf { it.isNotEmpty() }?.let {
            roles ->
            spec = spec.and(
                Specification<Task> { root, _, cb ->
                    // 커스텀 VO이지만, DB에는 단일 컬럼 employee_role 로 저장됨
                    val path = root.get<String>("employeeRoles")
                    // OR 조건 (roles 중 하나라도 포함되면 매칭)
                    val orPredicates = roles.map { role ->
                        cb.like(cb.lower(path), "%${role.lowercase()}%")
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
        val tasks = taskRepository.findAllBySpec(spec)

        // Task -> TaskResponseDTO 변환 후 반환
        return tasks.map { taskMapper.toResponseDto(it) }
    }

    /**
     * 특정 난이도 작업 조회 */
    @Transactional(readOnly = true)
    suspend fun findTasksByDifficulty(difficulty: Int): List<TaskResponseDTO> {

        if (difficulty < 0) {
            throw IllegalArgumentException("Difficulty must be non-negative")
        }

        val tasks = taskRepository.findAllByDifficulty(difficulty)
        return tasks.map { taskMapper.toResponseDto(it) }
    }
}
