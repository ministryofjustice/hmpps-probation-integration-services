package uk.gov.justice.digital.hmpps.listener

import feign.FeignException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.integrations.delius.service.DeliusService
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNotesClient
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonOffenderEvent
import uk.gov.justice.digital.hmpps.integrations.prison.toDeliusCaseNote
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Component
@EnableJms
class MessageListener(
    val prisonCaseNotesClient: PrisonCaseNotesClient,
    val deliusService: DeliusService,
    val telemetryService: TelemetryService
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @JmsListener(destination = "\${spring.jms.template.default-destination}")
    fun receive(prisonOffenderEvent: PrisonOffenderEvent) {
        if (prisonOffenderEvent.caseNoteId == null) {
            log.info("Received ${prisonOffenderEvent.eventType} for ${prisonOffenderEvent.offenderId} without a case note id")
            return
        }

        val prisonCaseNote = try {
            prisonCaseNotesClient.getCaseNote(prisonOffenderEvent.offenderId, prisonOffenderEvent.caseNoteId)
        } catch (notFound: FeignException.NotFound) {
            null
        }

        if (prisonCaseNote?.text == null || prisonCaseNote.text.isBlank()) {
            val reason = if (prisonCaseNote == null) "was not found" else "text is empty"
            log.warn(
                "Ignoring case note id {} and type {} because case note $reason",
                prisonOffenderEvent.caseNoteId,
                prisonOffenderEvent.eventType
            )
            return
        }

        log.debug(
            "Found case note {} of type {} {} in case notes service, now pushing to delius with event id {}",
            prisonOffenderEvent.caseNoteId,
            prisonCaseNote.type,
            prisonCaseNote.subType,
            prisonCaseNote.eventId
        )

        telemetryService.trackEvent(
            "CaseNoteMerge",
            mapOf(
                "caseNoteId" to prisonCaseNote.id,
                "type" to prisonCaseNote.type,
                "subType" to prisonCaseNote.subType,
                "eventId" to prisonCaseNote.eventId.toString(),
                "created" to DeliusDateTimeFormatter.format(prisonCaseNote.creationDateTime),
                "occurrence" to DeliusDateTimeFormatter.format(prisonCaseNote.occurrenceDateTime)
            )
        )

        deliusService.mergeCaseNote(prisonCaseNote.toDeliusCaseNote())
    }
}
