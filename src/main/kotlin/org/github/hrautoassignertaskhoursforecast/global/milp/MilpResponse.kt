package org.github.hrautoassignertaskhoursforecast.global.milp

//데이터를 불변값으로 단순히 받고호출 한다면 data class를 사용
data class MilpResponse(
    val tasks: List<String>,
    val teams: List<List<String>>,
    val durations: List<Double>
)