package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.recall.RecallService
import uk.gov.justice.digital.hmpps.integrations.delius.release.ReleaseService
import uk.gov.justice.digital.hmpps.message.AdditionalInformation
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived

@Component
class Handler(
    private val telemetryService: TelemetryService,
    private val releaseService: ReleaseService,
    private val recallService: RecallService,
    override val converter: NotificationConverter<HmppsDomainEvent>,
) : NotificationHandler<HmppsDomainEvent> {
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val hmppsEvent = notification.message

        try {
            when (notification.eventType) {
                "prison-offender-events.prisoner.released" -> {
                    releaseService.release(
                        hmppsEvent.additionalInformation.nomsNumber(),
                        hmppsEvent.additionalInformation.prisonId(),
                        hmppsEvent.additionalInformation.reason(),
                        hmppsEvent.occurredAt,
                    )
                    telemetryService.trackEvent("PrisonerReleased", hmppsEvent.telemetryProperties())
                }

                "prison-offender-events.prisoner.received" -> {
                    recallService.recall(
                        hmppsEvent.additionalInformation.nomsNumber(),
                        hmppsEvent.additionalInformation.prisonId(),
                        hmppsEvent.additionalInformation.reason(),
                        hmppsEvent.occurredAt,
                    )
                    telemetryService.trackEvent("PrisonerRecalled", hmppsEvent.telemetryProperties())
                }

                else -> throw IllegalArgumentException("Unknown event type ${notification.eventType}")
            }
        } catch (e: IgnorableMessageException) {
            telemetryService.trackEvent(e.message, hmppsEvent.telemetryProperties() + e.additionalProperties)
            return
        }
    }
}

fun AdditionalInformation.nomsNumber() = this["nomsNumber"] as String
fun AdditionalInformation.prisonId() = this["prisonId"] as String
fun AdditionalInformation.reason() = this["reason"] as String
fun AdditionalInformation.details() = this["details"] as String?
fun HmppsDomainEvent.telemetryProperties() = listOfNotNull(
    "occurredAt" to occurredAt.toString(),
    "nomsNumber" to additionalInformation.nomsNumber(),
    "institution" to additionalInformation.prisonId(),
    "reason" to additionalInformation.reason(),
    additionalInformation.details()?.let { "details" to it }
).toMap()
