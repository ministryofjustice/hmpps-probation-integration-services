package uk.gov.justice.digital.hmpps.messaging

import feign.FeignException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.courtcase.CourtCaseClient
import uk.gov.justice.digital.hmpps.integrations.courtcase.CourtCaseNote
import uk.gov.justice.digital.hmpps.integrations.delius.service.DeliusIntegrationService
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
import java.net.URI

@Component
class Handler(
    val courtCaseClient: CourtCaseClient,
    val deliusIntegrationService: DeliusIntegrationService,
    val telemetryService: TelemetryService,
    override val converter: NotificationConverter<HmppsDomainEvent>
) : NotificationHandler<HmppsDomainEvent> {

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val event = notification.message
        val crn = event.personReference.findCrn()

        val courtCaseNote = try {
            courtCaseClient.getCourtCaseNote(URI.create(event.detailUrl!!))
        } catch (notFound: FeignException.NotFound) {
            telemetryService.trackEvent("CourtCaseNoteNotFound", mapOf("detailUrl" to event.detailUrl!!))
            return
        }

        if (courtCaseNote == null) {
            log.warn(
                "Ignoring case note for crn {} and type {} because court case note was not found",
                crn,
                notification.eventType
            )
            return
        }

        log.debug(
            "Found court case note in court case service for crn {}, now pushing to delius",
            crn
        )

        try {
            deliusIntegrationService.mergeCourtCaseNote(crn!!, courtCaseNote, notification.message.occurredAt)
            telemetryService.trackEvent("CourtCaseNoteMerged", courtCaseNote.properties())
        } catch (e: Exception) {
            telemetryService.trackEvent(
                "CourtCaseNoteMergeFailed",
                courtCaseNote.properties() + ("exception" to (e.message ?: ""))
            )
            if (e !is NotFoundException) throw e
        }
    }

    private fun CourtCaseNote.properties() = mapOf(
        "externalReference" to reference
    )
}
