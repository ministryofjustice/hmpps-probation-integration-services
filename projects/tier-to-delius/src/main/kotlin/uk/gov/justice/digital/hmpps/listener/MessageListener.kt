package uk.gov.justice.digital.hmpps.listener

import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.integrations.tier.TierCalculation
import uk.gov.justice.digital.hmpps.integrations.tier.TierClient
import uk.gov.justice.digital.hmpps.integrations.tier.TierService
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
import java.net.URI

@Component
@EnableJms
class MessageListener(
    private val telemetryService: TelemetryService,
    private val tierClient: TierClient,
    private val tierService: TierService,
) {
    @JmsListener(destination = "\${spring.jms.template.default-destination}")
    fun receive(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val crn = notification.message.personReference.findCrn()!!
        val tierCalculation = tierClient.getTierCalculation(URI.create(notification.message.detailUrl!!))
        tierService.updateTier(crn, tierCalculation)
        telemetryService.trackEvent("TierUpdateSuccess", tierCalculation.telemetryProperties(crn))
    }
}

fun TierCalculation.telemetryProperties(crn: String) = mapOf(
    "crn" to crn,
    "tier" to tierScore,
    "calculationDate" to calculationDate.toString()
)
