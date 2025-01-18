package org.github.hrautoassignertaskhoursforecast.Task.domain.entity

import jakarta.persistence.*
import org.github.hrautoassignertaskhoursforecast.Task.application.dto.TaskResponseDTO
import org.github.hrautoassignertaskhoursforecast.global.RoleType
import org.github.hrautoassignertaskhoursforecast.Task.domain.entity.vo.EmployeeRolesConverter
import org.github.hrautoassignertaskhoursforecast.Task.domain.entity.vo.EmployeeRoles
import org.github.hrautoassignertaskhoursforecast.Task.domain.entity.vo.RequirementsConverter
import org.github.hrautoassignertaskhoursforecast.Task.domain.entity.vo.Requirements

@Entity
@Table(name = "tasks")
data class Task(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long= 0L,

    @Column(name = "name", nullable = false)
    var taskName: String,

    @Convert(converter = EmployeeRolesConverter::class)
    @Column(name = "employee_role")
    var employeeRoles: EmployeeRoles = EmployeeRoles(""),

    @Column(name = "difficulty", nullable = false)
    var difficulty: Int = 1,

    /**
     * DB에는 쉼표 구분 문자열,
     * 코드에는 Requirements(=List<String>)로 사용 */
    @Convert(converter = RequirementsConverter::class)
    @Column(name = "requirements")
    var requirements: Requirements? = null,

) {

    fun isValid(): Boolean {
        return taskName.isNotBlank() && difficulty in 1..5
    }

    fun updateTask(
        name: String,
        difficulty: Int,
        employeeRoles: List<RoleType>,
        requirementsList: List<String> // 변경할 요구사항 리스트
    ) {
        this.taskName = name
        this.difficulty = difficulty

        // EmployeeRoles VO 업데이트
        val newEmployeeRoles = EmployeeRoles()
        newEmployeeRoles.setRoles(employeeRoles)
        this.employeeRoles = newEmployeeRoles

        // Requirements VO 업데이트
        val newRequirements = Requirements()
        newRequirements.setRequirements(requirementsList)
        this.requirements = newRequirements
    }

    fun toResponseDto(): TaskResponseDTO {
        return TaskResponseDTO(
            id = this.id,
            taskName = this.taskName,
            difficulty = this.difficulty,
            // EmployeeRoles → List<String>
            employeeRoles = this.employeeRoles.roles.map { it.displayName },
            // Requirements → List<String>
            // null일 수 있으므로, null이면 빈 리스트로 처리
            requirements = this.requirements?.requirementsList ?: emptyList()
        )
    }
}