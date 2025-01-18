package org.github.hrautoassignertaskhoursforecast.TaskHistory.domain.vo

data class TeamMembersVO(
    val members: List<String>
) {
    //모든 멤버를 쉼표로 구분된 문자열로 반환
    fun toCommaSeparatedString(): String {
        return members.joinToString(", ")
    }

}