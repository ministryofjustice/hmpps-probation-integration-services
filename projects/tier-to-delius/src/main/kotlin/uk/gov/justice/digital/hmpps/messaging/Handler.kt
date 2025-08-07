package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.asyncapi.kotlinasyncapi.annotation.channel.Message
import com.asyncapi.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.detail.DomainEventDetailService
import uk.gov.justice.digital.hmpps.integrations.tier.TierCalculation
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.service.TierUpdateService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Component
@Channel("tier-to-delius-queue")
class Handler(
    private val telemetryService: TelemetryService,
    private val detailService: DomainEventDetailService,
    private val tierUpdateService: TierUpdateService,
    override val converter: NotificationConverter<HmppsDomainEvent>
) : NotificationHandler<HmppsDomainEvent> {
    @Publish(messages = [Message(name = "tiering/tier_calculation_complete")])
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val crn = requireNotNull(notification.message.personReference.findCrn())
        val detailUrl = requireNotNull(notification.message.detailUrl)
        val tierCalculation = try {
            detailService.getDetail<TierCalculation>(notification.message)
        } catch (_: HttpClientErrorException.NotFound) {
            telemetryService.trackEvent("TierCalculationNotFound", mapOf("crn" to crn, "detailUrl" to detailUrl))
            return
        }
        tierUpdateService.updateTier(crn, tierCalculation)
        telemetryService.trackEvent("TierUpdateSuccess", tierCalculation.telemetryProperties(crn))
    }
}

fun TierCalculation.telemetryProperties(crn: String) = mapOf(
    "crn" to crn,
    "tier" to tierScore,
    "calculationDate" to calculationDate.toString()
)
