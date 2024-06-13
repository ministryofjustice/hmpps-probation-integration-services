package uk.gov.justice.digital.hmpps.messaging

import org.openfolder.kotlinasyncapi.annotation.channel.Channel
import org.openfolder.kotlinasyncapi.annotation.channel.Message
import org.openfolder.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.integrations.tier.TierCalculation
import uk.gov.justice.digital.hmpps.integrations.tier.TierClient
import uk.gov.justice.digital.hmpps.integrations.tier.TierService
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
import java.net.URI

@Component
@Channel("tier-to-delius-queue")
class Handler(
    private val telemetryService: TelemetryService,
    private val tierClient: TierClient,
    private val tierService: TierService,
    override val converter: NotificationConverter<HmppsDomainEvent>
) : NotificationHandler<HmppsDomainEvent> {
    @Publish(messages = [Message(name = "tiering/tier_calculation_complete")])
    override fun handle(notification: Notification<HmppsDomainEvent>) {
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
