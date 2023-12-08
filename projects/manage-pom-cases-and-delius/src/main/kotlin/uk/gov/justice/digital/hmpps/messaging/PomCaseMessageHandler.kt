package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.services.HandoverDatesChanged
import uk.gov.justice.digital.hmpps.services.PomAllocated
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived

@Component
class PomCaseMessageHandler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val handoverDatesChanged: HandoverDatesChanged,
    private val pomAllocated: PomAllocated,
    private val telemetryService: TelemetryService,
) : NotificationHandler<HmppsDomainEvent> {
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        when (notification.eventType) {
            "offender-management.handover.changed" -> handoverDatesChanged.process(notification.message)
            "offender-management.allocation.changed" -> pomAllocated.process(notification.message)
            else -> throw NotImplementedError("Unhandled message type received: ${notification.eventType}")
        }
    }
}
