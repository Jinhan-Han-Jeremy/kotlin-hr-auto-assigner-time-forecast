package org.github.hrautoassignertaskhoursforecast

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories(basePackages = ["org.github.hrautoassignertaskhoursforecast"])
class HrAutoAssignerTaskHoursForecastApplication

fun main(args: Array<String>) {
    runApplication<HrAutoAssignerTaskHoursForecastApplication>(*args)
}
