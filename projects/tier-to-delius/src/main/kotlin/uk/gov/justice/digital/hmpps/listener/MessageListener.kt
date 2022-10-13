package uk.gov.justice.digital.hmpps.listener

import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.integrations.tier.TierCalculation
import uk.gov.justice.digital.hmpps.integrations.tier.TierClient
import uk.gov.justice.digital.hmpps.integrations.tier.TierService
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Component
@EnableJms
class MessageListener(
    private val telemetryService: TelemetryService,
    private val tierClient: TierClient,
    private val tierService: TierService,
) {
    @JmsListener(destination = "\${spring.jms.template.default-destination}")
    fun receive(notification: Notification<TierChangeEvent>) {
        telemetryService.notificationReceived(notification)
        val crn = notification.message.crn
        val calculationId = notification.message.calculationId
        val tierCalculation = tierClient.getTierCalculation(crn, calculationId)
        tierService.updateTier(crn, tierCalculation)
        telemetryService.trackEvent("TierUpdateSuccess", tierCalculation.telemetryProperties(crn))
    }
}

fun TierCalculation.telemetryProperties(crn: String) = mapOf(
    "crn" to crn,
    "tier" to tierScore,
    "calculationDate" to calculationDate.toString()
)
