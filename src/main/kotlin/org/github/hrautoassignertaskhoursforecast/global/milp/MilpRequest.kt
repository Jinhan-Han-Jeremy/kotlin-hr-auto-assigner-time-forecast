package org.github.hrautoassignertaskhoursforecast.global.milp

import com.fasterxml.jackson.annotation.JsonProperty

data class MilpRequest(
    val tasks: List<String>,

    @JsonProperty("member_performances")  // FastAPI의 스네이크 케이스와 맞춤
    val memberPerformances: List<Map<String, Int>>
)