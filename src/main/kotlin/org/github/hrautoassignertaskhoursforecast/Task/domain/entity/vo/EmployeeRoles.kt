package org.github.hrautoassignertaskhoursforecast.Task.domain.entity.vo

import org.github.hrautoassignertaskhoursforecast.global.RoleType

/**
 * DB에는 쉼표 구분 문자열 형태로 저장하되,*/
data class EmployeeRoles(
    // DB에 저장되는 원본 문자열(예: "ProjectManager, BusinessOperator")
    private var rolesRaw: String = ""
) {
    /**
     * roles 필드:
     *  - rolesRaw 문자열을 쉼표로 split하여
     *  - RoleType으로 매핑 */
    val roles: List<RoleType>
        get() {
            if (rolesRaw.isBlank()) return emptyList()
            return rolesRaw.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { roleString ->
                    // RoleType enum 안의 displayName과 대소문자 구분 없이 매칭
                    RoleType.values().find { it.displayName.equals(roleString, ignoreCase = true) }
                        ?: throw IllegalArgumentException("Invalid role: $roleString")
                }
        }

    /**
     * VO 내부에 저장된 쉼표 구분 문자열 반환 */
    fun getRolesStr(): String {
        return rolesRaw
    }

    /**
     * RoleType 리스트를 받아서
     * 다시 쉼표 구분 문자열로 저장 */
    fun setRoles(newRoles: List<RoleType>) {
        rolesRaw = newRoles.joinToString(",") { it.displayName }
    }

    /**
     * 문자열을 직접 넣을 때 사용 */
    fun setRolesRaw(rawString: String) {
        rolesRaw = rawString
    }


}