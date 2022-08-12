package uk.gov.justice.digital.hmpps.listener

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentService
import uk.gov.justice.digital.hmpps.integrations.psr.PsrClient
import uk.gov.justice.digital.hmpps.message.HmppsEvent
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.net.URI

@Component
@EnableJms
class MessageListener(
    private val telemetryService: TelemetryService,
    private val psrClient: PsrClient,
    private val documentService: DocumentService
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @JmsListener(destination = "\${spring.jms.template.default-destination}")
    fun receive(hmppsEvent: HmppsEvent) {
        log.debug("received $hmppsEvent")
        telemetryService.hmppsEventReceived(hmppsEvent)
        val psrDocument = psrClient.getPsrReport(URI.create(hmppsEvent.detailUrl + "/pdf"))
        documentService.updateCourtReportDocument(hmppsEvent, psrDocument)
    }
}
