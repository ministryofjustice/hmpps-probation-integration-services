package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.asyncapi.kotlinasyncapi.annotation.channel.Message
import com.asyncapi.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.detail.DomainEventDetailService
import uk.gov.justice.digital.hmpps.integration.ReferralCompletion
import uk.gov.justice.digital.hmpps.integration.StatusInfo
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.service.ComponentTerminationService
import uk.gov.justice.digital.hmpps.service.StatusChangeService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Component
@Channel("accredited-programmes-and-delius-queue")
class Handler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val detailService: DomainEventDetailService,
    private val statusChangeService: StatusChangeService,
    private val componentTerminationService: ComponentTerminationService,
    private val telemetryService: TelemetryService
) : NotificationHandler<HmppsDomainEvent> {
    @Publish(messages = [Message(title = REFERRAL_STATUS_CHANGED), Message(title = REFERRAL_COMPLETE)])
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val domainEvent = notification.message
        val crn = requireNotNull(domainEvent.personReference.findCrn())

        when (notification.eventType) {
            REFERRAL_STATUS_CHANGED -> detailService.getDetail<StatusInfo>(domainEvent.detailUrl).also { detail ->
                statusChangeService.statusChanged(notification.id, crn, domainEvent.occurredAt, detail)
                telemetryService.trackEvent(
                    "StatusChanged",
                    mapOf(
                        "crn" to crn,
                        "componentId" to detail.sourcedFromEntityId.toString(),
                        "status" to detail.newStatus.toString(),
                    )
                )
            }

            REFERRAL_COMPLETE -> detailService.getDetail<ReferralCompletion>(domainEvent.detailUrl).also { detail ->
                val result = componentTerminationService.terminate(crn, detail)
                telemetryService.trackEvent(result.name.toString(), result.data)
            }

            else -> error("Unexpected event type ${notification.eventType}")
        }
    }

    companion object {
        const val REFERRAL_STATUS_CHANGED = "accredited-programmes-community.referral.status-updated"
        const val REFERRAL_COMPLETE = "accredited-programmes-community.programme.complete"
    }
}
