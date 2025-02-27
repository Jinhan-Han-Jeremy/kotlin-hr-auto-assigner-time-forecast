package org.github.hrautoassignertaskhoursforecast.task.domain.entity.vo

data class Requirements(
    private var requirementsRaw: String? = ""
) {
    val requirementsList: List<String>
        get() = requirementsRaw
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

    fun getRequirementContent(): String? = requirementsRaw

    fun setRequirements(newRequirements: List<String>) {
        requirementsRaw = newRequirements.joinToString(",") { it.trim() }
    }

    override fun toString(): String = requirementsList.joinToString(",")
}