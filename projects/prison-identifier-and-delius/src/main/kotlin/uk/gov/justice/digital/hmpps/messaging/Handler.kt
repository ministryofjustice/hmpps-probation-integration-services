package uk.gov.justice.digital.hmpps.messaging

import org.openfolder.kotlinasyncapi.annotation.Schema
import org.openfolder.kotlinasyncapi.annotation.channel.Channel
import org.openfolder.kotlinasyncapi.annotation.channel.Message
import org.openfolder.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.model.logResult
import uk.gov.justice.digital.hmpps.service.PrisonMatchingService
import uk.gov.justice.digital.hmpps.service.ProbationMatchingService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Component
@Channel("prison-identifier-and-delius-queue")
class Handler(
    override val converter: NotificationConverter<Any>,
    private val telemetryService: TelemetryService,
    private val probationMatchingService: ProbationMatchingService,
    private val prisonMatchingService: PrisonMatchingService,
) : NotificationHandler<Any> {
    @Publish(
        messages = [
            Message(
                messageId = "prison-offender-events.prisoner.sentence-dates-changed",
                payload = Schema(HmppsDomainEvent::class)
            ),
            Message(
                messageId = "prison-offender-events.prisoner.imprisonment-status-changed",
                payload = Schema(HmppsDomainEvent::class)
            ),
            Message(messageId = "prison-offender-events.prisoner.merged", payload = Schema(HmppsDomainEvent::class)),
            Message(messageId = "OFFENDER_DETAILS_CHANGED", payload = Schema(OffenderEvent::class)),
            Message(messageId = "SENTENCE_CHANGED", payload = Schema(OffenderEvent::class)),
            Message(
                messageId = "prison-identifier.internal.prison-match-requested",
                summary = "Internal use - attempt to match a case",
                payload = Schema(HmppsDomainEvent::class)
            ),
            Message(
                messageId = "prison-identifier.internal.probation-match-requested",
                summary = "Internal use - attempt to match a case",
                payload = Schema(HmppsDomainEvent::class)
            ),
        ]
    )
    override fun handle(notification: Notification<Any>) {
        telemetryService.notificationReceived(notification)
        when (val message = notification.message) {
            is HmppsDomainEvent -> when (notification.eventType) {
                "prison-identifier.internal.prison-match-requested" -> prisonMatchingService
                    .matchAndUpdateIdentifiers(checkNotNull(message.personReference.findCrn()), message.dryRun)
                    .also { telemetryService.logResult(it, message.dryRun) }

                "prison-identifier.internal.probation-match-requested" -> probationMatchingService
                    .matchAndUpdateIdentifiers(checkNotNull(message.personReference.findNomsNumber()), message.dryRun)
                    .also { telemetryService.logResult(it, message.dryRun) }

                "prison-offender-events.prisoner.sentence-dates-changed",
                "prison-offender-events.prisoner.imprisonment-status-changed" -> probationMatchingService
                    .matchAndUpdateIdentifiers(checkNotNull(message.personReference.findNomsNumber()))
                    .also { telemetryService.logResult(it) }

                "prison-offender-events.prisoner.merged" -> probationMatchingService
                    .replaceIdentifiers(message.oldNoms, message.newNoms)
                    .also { telemetryService.logResult(it) }

                else -> throw IllegalArgumentException("Unexpected domain event type: ${notification.eventType}")
            }

            is OffenderEvent -> when (notification.eventType) {
                "OFFENDER_DETAILS_CHANGED", // changes to name, date of birth, identifiers in Delius
                "SENTENCE_CHANGED",         // changes to sentence status and dates in Delius
                -> prisonMatchingService
                    .matchAndUpdateIdentifiers(message.crn)
                    .also { telemetryService.logResult(it) }

                else -> throw IllegalArgumentException("Unexpected offender event type: ${notification.eventType}")
            }

            else -> throw IllegalArgumentException("Unexpected event type: ${notification.eventType}")
        }
    }

    val HmppsDomainEvent.dryRun get() = additionalInformation["dryRun"] == true
    val HmppsDomainEvent.oldNoms get() = checkNotNull(additionalInformation["removedNomsNumber"]) as String
    val HmppsDomainEvent.newNoms get() = checkNotNull(additionalInformation["nomsNumber"]) as String
}
