package uk.gov.justice.digital.hmpps.messaging

import org.openfolder.kotlinasyncapi.annotation.channel.Channel
import org.openfolder.kotlinasyncapi.annotation.channel.Message
import org.openfolder.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.services.HandoverDatesChanged
import uk.gov.justice.digital.hmpps.services.PomAllocated
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived

@Component
@Channel("manage-pom-cases-and-delius-queue")
class PomCaseMessageHandler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val handoverDatesChanged: HandoverDatesChanged,
    private val pomAllocated: PomAllocated,
    private val telemetryService: TelemetryService
) : NotificationHandler<HmppsDomainEvent> {
    @Publish(
        messages = [
            Message(name = "offender-management/handover-changed"),
            Message(name = "offender-management/pom-allocated")
        ]
    )
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        when (notification.eventType) {
            "offender-management.handover.changed" -> handoverDatesChanged.process(notification.message)
            "offender-management.allocation.changed" -> pomAllocated.process(notification.message)
            else -> throw NotImplementedError("Unhandled message type received: ${notification.eventType}")
        }
    }
}
