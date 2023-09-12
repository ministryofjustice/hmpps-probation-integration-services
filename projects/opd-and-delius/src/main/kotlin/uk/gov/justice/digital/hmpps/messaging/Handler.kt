package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.integrations.opd.OPDClient
import uk.gov.justice.digital.hmpps.integrations.opd.OPDService
import uk.gov.justice.digital.hmpps.integrations.opd.telemetryProperties
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
import java.net.URI

@Component
class Handler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val telemetryService: TelemetryService,
    private val opdClient: OPDClient,
    private val opdService: OPDService
) : NotificationHandler<HmppsDomainEvent> {
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val crn = notification.message.personReference.findCrn()!!
        val opdAssessment = opdClient.getOPDAssessment(URI.create(notification.message.detailUrl!!))
        opdService.processAssessment(crn, opdAssessment)
        telemetryService.trackEvent("TierUpdateSuccess", opdAssessment.telemetryProperties(crn))
    }
}
