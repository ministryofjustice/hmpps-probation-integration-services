package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.asyncapi.kotlinasyncapi.annotation.channel.Message
import com.asyncapi.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.digital.hmpps.client.TierClient
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.tier.TierCalculationV2
import uk.gov.justice.digital.hmpps.integrations.tier.TierCalculationV3
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.service.TierUpdateService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Component
@Channel("tier-to-delius-queue")
class Handler(
    private val telemetryService: TelemetryService,
    private val tierUpdateService: TierUpdateService,
    private val featureFlags: FeatureFlags,
    private val tierClient: TierClient,
    override val converter: NotificationConverter<HmppsDomainEvent>,
) : NotificationHandler<HmppsDomainEvent> {
    @Transactional
    @Publish(messages = [Message(name = "tiering/tier_calculation_complete")])
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val (tier2, tier3) = try {
            Pair(
                tierClient.tierV2(notification.crn, notification.calculationId),
                tierClient.tierV3(notification.crn, notification.calculationId)
            )
        } catch (_: HttpClientErrorException.NotFound) {
            telemetryService.trackEvent("TierCalculationNotFound", notification.telemetry)
            return
        }
        try {
            if (featureFlags.enabled("tier-v3-delius-phase-1")) {
                // Phase 1 - update the offender.v3_tier_id column
                tierUpdateService.updateV3TierColumn(notification.crn, tier3)
                telemetryService.trackEvent("TierV3UpdateSuccess", notification.telemetry + tier3.telemetry)
            }
            if (featureFlags.enabled("tier-v3-delius-phase-2")) {
                // Phase 2 - populate the offender.current_tier column and create the management_tier history record
                tierUpdateService.updateTier(notification.crn, tier3)
                telemetryService.trackEvent("TierUpdateSuccess", notification.telemetry + tier3.telemetry)
            } else {
                tierUpdateService.updateTier(notification.crn, tier2)
                telemetryService.trackEvent("TierUpdateSuccess", notification.telemetry + tier2.telemetry)
            }
        } catch (e: IgnorableMessageException) {
            telemetryService
                .trackEvent(e.message, notification.telemetry + tier2.telemetry + tier3.telemetry)
        }
    }

    val Notification<HmppsDomainEvent>.crn get() = requireNotNull(message.personReference.findCrn())
    val Notification<HmppsDomainEvent>.calculationId get() = requireNotNull(message.additionalInformation["calculationId"] as String?)

    val Notification<HmppsDomainEvent>.telemetry
        get() = mapOf(
            "crn" to crn,
            "calculationId" to calculationId,
        )

    val TierCalculationV2.telemetry
        get() = mapOf(
            "tierV2" to tierScore,
            "calculationId" to calculationId,
            "calculationDate" to calculationDate.toString()
        )

    val TierCalculationV3.telemetry
        get() = mapOf(
            "tierV3" to tierScore,
            "provisional" to provisional.toString(),
            "calculationId" to calculationId,
            "calculationDate" to calculationDate.toString()
        )
}