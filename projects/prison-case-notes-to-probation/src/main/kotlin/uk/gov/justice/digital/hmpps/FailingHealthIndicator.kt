package uk.gov.justice.digital.hmpps

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component

@Component
class FailingHealthIndicator : HealthIndicator {
    override fun health(): Health {
        throw RuntimeException("testing")
    }
}
