package org.github.hrautoassignertaskhoursforecast.taskHistory.domain.vo

data class TeamMembersVO(
    val membersNames: List<String>
) {
    //모든 멤버를 쉼표로 구분된 문자열로 반환
    fun toCommaSeparatedString(): String {
        return membersNames.joinToString(", ")
    }

}