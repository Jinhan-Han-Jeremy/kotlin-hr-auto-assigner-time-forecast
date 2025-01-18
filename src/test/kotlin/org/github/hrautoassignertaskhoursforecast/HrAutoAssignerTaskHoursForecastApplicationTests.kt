package org.github.hrautoassignertaskhoursforecast

import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EntityScan("org.github.hrautoassignertaskhoursforecast")
@EnableJpaRepositories("org.github.hrautoassignertaskhoursforecast")
@SpringBootTest
class HrAutoAssignerTaskHoursForecastApplicationTests {

//    @Test
//    fun contextLoads() {
//    }

}
