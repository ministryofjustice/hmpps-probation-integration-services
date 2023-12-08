package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.integrations.makerecalldecisions.MakeRecallDecisionsClient
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.service.ManagementOversightRecall
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
import java.net.URI

@Component
class Handler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val managementOversightRecall: ManagementOversightRecall,
    private val makeRecallDecisionsClient: MakeRecallDecisionsClient,
    private val telemetryService: TelemetryService,
) : NotificationHandler<HmppsDomainEvent> {
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val crn =
            notification.message.personReference.findCrn()
                ?: throw IllegalArgumentException("CRN not found in message")
        when (notification.eventType) {
            "prison-recall.recommendation.management-oversight" ->
                managementOversightRecall.decision(
                    crn = crn,
                    decision = notification.decision(),
                    details = notification.details(),
                    username = notification.bookedByUsername(),
                    occurredAt = notification.message.occurredAt,
                )

            else -> throw NotImplementedError("Unhandled message type received: ${notification.eventType}")
        }
    }

    private fun Notification<HmppsDomainEvent>.details() = makeRecallDecisionsClient.getDetails(detailUrl())
}

private fun Notification<HmppsDomainEvent>.detailUrl() =
    message.detailUrl?.takeIf { it.isNotBlank() }?.let { URI(it) }
        ?: throw IllegalArgumentException("No detail url provided")

private fun Notification<HmppsDomainEvent>.decision() = ManagementDecision.valueOf(message.additionalInformation["contactOutcome"] as String)

private fun Notification<HmppsDomainEvent>.bookedByUsername(): String =
    (message.additionalInformation["bookedBy"] as Map<*, *>)["username"] as String?
        ?: throw IllegalArgumentException("No Staff Code present in message")
