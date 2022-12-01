package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentService
import uk.gov.justice.digital.hmpps.integrations.psr.PsrClient
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
import java.net.URI

@Component
class Handler(
    private val telemetryService: TelemetryService,
    private val psrClient: PsrClient,
    private val documentService: DocumentService
) : NotificationHandler<HmppsDomainEvent> {
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val hmppsEvent = notification.message
        val psrDocument = psrClient.getPsrReport(URI.create(hmppsEvent.detailUrl!! + "/pdf"))
        documentService.updateCourtReportDocument(hmppsEvent, psrDocument)
    }

    override fun getMessageType() = HmppsDomainEvent::class
}
