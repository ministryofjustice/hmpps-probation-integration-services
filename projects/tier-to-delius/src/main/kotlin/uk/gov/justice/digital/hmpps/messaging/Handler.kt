package uk.gov.justice.digital.hmpps.messaging

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
class Handler(
    private val telemetryService: TelemetryService,
    private val tierClient: TierClient,
    private val tierService: TierService,
) : NotificationHandler<HmppsDomainEvent> {
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val crn = notification.message.personReference.findCrn()!!
        val tierCalculation = tierClient.getTierCalculation(URI.create(notification.message.detailUrl!!))
        tierService.updateTier(crn, tierCalculation)
        telemetryService.trackEvent("TierUpdateSuccess", tierCalculation.telemetryProperties(crn))
    }

    override fun getMessageType() = HmppsDomainEvent::class
}

fun TierCalculation.telemetryProperties(crn: String) = mapOf(
    "crn" to crn,
    "tier" to tierScore,
    "calculationDate" to calculationDate.toString()
)
