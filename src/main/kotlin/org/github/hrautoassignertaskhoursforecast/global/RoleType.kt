package org.github.hrautoassignertaskhoursforecast.global

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class RoleType(val displayName: String) {
    PROJECT_MANAGER("ProjectManager"),
    PRODUCT_MANAGER("ProductManager"),
    BUSINESS_OPERATOR("BusinessOperator"),
    DEVELOPER("Developer"),
    TECH_LEAD("TechLead"),
    DESIGNER("Designer"),
    DEVOPS_ENGINEER("DevOpsEngineer");


    //문자열 형태로 변환
    override fun toString(): String {
        return displayName
    }
}