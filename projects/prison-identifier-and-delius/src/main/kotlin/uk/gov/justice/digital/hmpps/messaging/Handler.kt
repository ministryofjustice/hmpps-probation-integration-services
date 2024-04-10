package uk.gov.justice.digital.hmpps.messaging

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.model.logResult
import uk.gov.justice.digital.hmpps.service.PrisonMatchingService
import uk.gov.justice.digital.hmpps.service.ProbationMatchingService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived

@Component
class Handler(
    override val converter: NotificationConverter<Any>,
    private val telemetryService: TelemetryService,
    private val probationMatchingService: ProbationMatchingService,
    private val prisonMatchingService: PrisonMatchingService,
    @Value("\${messaging.consumer.dry-run:true}") private val messagingDryRun: Boolean
) : NotificationHandler<Any> {
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
                    .matchAndUpdateIdentifiers(checkNotNull(message.personReference.findNomsNumber()), messagingDryRun)
                    .also { telemetryService.logResult(it, messagingDryRun) }

                "prison-offender-events.prisoner.merged" -> probationMatchingService
                    .replaceIdentifiers(message.oldNoms, message.newNoms, messagingDryRun)
                    .also { telemetryService.logResult(it, messagingDryRun) }

                else -> throw IllegalArgumentException("Unexpected domain event type: ${notification.eventType}")
            }

            is OffenderEvent -> when (notification.eventType) {
                "SENTENCE_CHANGED" -> prisonMatchingService
                    .matchAndUpdateIdentifiers(message.crn, messagingDryRun)
                    .also { telemetryService.logResult(it, messagingDryRun) }

                else -> throw IllegalArgumentException("Unexpected offender event type: ${notification.eventType}")
            }

            else -> throw IllegalArgumentException("Unexpected event type: ${notification.eventType}")
        }
    }

    val HmppsDomainEvent.dryRun get() = additionalInformation["dryRun"] == true
    val HmppsDomainEvent.oldNoms get() = checkNotNull(additionalInformation["removedNomsNumber"]) as String
    val HmppsDomainEvent.newNoms get() = checkNotNull(additionalInformation["nomsNumber"]) as String
}
