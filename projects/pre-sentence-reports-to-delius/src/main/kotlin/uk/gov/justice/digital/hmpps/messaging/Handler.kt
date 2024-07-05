package uk.gov.justice.digital.hmpps.messaging

import org.openfolder.kotlinasyncapi.annotation.channel.Channel
import org.openfolder.kotlinasyncapi.annotation.channel.Message
import org.openfolder.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentService
import uk.gov.justice.digital.hmpps.integrations.psr.PsrClient
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.net.URI

@Component
@Channel("pre-sentence-reports-to-delius-queue")
class Handler(
    private val telemetryService: TelemetryService,
    private val psrClient: PsrClient,
    private val documentService: DocumentService,
    override val converter: NotificationConverter<HmppsDomainEvent>
) : NotificationHandler<HmppsDomainEvent> {
    @Publish(messages = [Message(name = "pre-sentence/pre-sentence_report_completed")])
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val hmppsEvent = notification.message
        val psrDocument = psrClient.getPsrReport(URI.create(hmppsEvent.detailUrl!! + "/pdf"))
        documentService.updateCourtReportDocument(hmppsEvent, psrDocument)
    }
}
