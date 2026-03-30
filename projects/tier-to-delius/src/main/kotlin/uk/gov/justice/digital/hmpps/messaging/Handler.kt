package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.asyncapi.kotlinasyncapi.annotation.channel.Message
import com.asyncapi.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.digital.hmpps.client.TierClient
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.detail.DomainEventDetailService
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
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
    private val featureFlags: FeatureFlags,
    private val tierClient: TierClient,
    override val converter: NotificationConverter<HmppsDomainEvent>,
) : NotificationHandler<HmppsDomainEvent> {
    @Publish(messages = [Message(name = "tiering/tier_calculation_complete")])
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val tierCalculation = try {
            if (featureFlags.enabled("tier-to-delius-v3")) {
                tierClient.tierV3(notification.crn, notification.calculationId)
            } else {
                tierClient.tierV2(notification.crn, notification.calculationId)
            }
        } catch (_: HttpClientErrorException.NotFound) {
            telemetryService.trackEvent("TierCalculationNotFound", notification.telemetry)
            return
        }
        try {
            tierUpdateService.updateTier(notification.crn, tierCalculation)
            telemetryService.trackEvent("TierUpdateSuccess", notification.telemetry + tierCalculation.telemetry)
        } catch (e: IgnorableMessageException) {
            telemetryService.trackEvent(e.message, notification.telemetry + tierCalculation.telemetry)
        }
    }

    val Notification<HmppsDomainEvent>.crn get() = requireNotNull(message.personReference.findCrn())
    val Notification<HmppsDomainEvent>.calculationId get() = requireNotNull(message.additionalInformation["calculationId"] as String?)

    val Notification<HmppsDomainEvent>.telemetry
        get() = mapOf(
            "crn" to crn,
            "calculationId" to calculationId,
        )

    val TierCalculation.telemetry
        get() = mapOf(
            "tier" to tierScore,
            "calculationId" to calculationId,
            "calculationDate" to calculationDate.toString()
        )
}