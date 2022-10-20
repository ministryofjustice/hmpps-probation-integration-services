package uk.gov.justice.digital.hmpps.listener

import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentService
import uk.gov.justice.digital.hmpps.integrations.psr.PsrClient
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
import java.net.URI

@Component
@EnableJms
class MessageListener(
    private val telemetryService: TelemetryService,
    private val psrClient: PsrClient,
    private val documentService: DocumentService
) {
    @JmsListener(destination = "\${spring.jms.template.default-destination}")
    fun receive(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val hmppsEvent = notification.message
        val psrDocument = psrClient.getPsrReport(URI.create(hmppsEvent.detailUrl!! + "/pdf"))
        documentService.updateCourtReportDocument(hmppsEvent, psrDocument)
    }
}
