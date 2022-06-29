package uk.gov.justice.digital.hmpps.listener

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.config.TelemetryService
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.integrations.delius.service.DeliusService
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNotesClient
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonOffenderEvent
import uk.gov.justice.digital.hmpps.integrations.prison.toDeliusCaseNote

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
            log.info("Received an event for ${prisonOffenderEvent.offenderId} without a case note id: ${prisonOffenderEvent.eventType}")
            return
        }

        val prisonCaseNote = prisonCaseNotesClient.getCaseNote(prisonOffenderEvent.offenderId, prisonOffenderEvent.caseNoteId)

        if (prisonCaseNote.text.isBlank()) {
            log.warn(
                "Ignoring case note id {} and type {} because case note text is empty",
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
                "caseNoteId" to prisonOffenderEvent.caseNoteId.toString(),
                "type" to "${prisonCaseNote.type}-${prisonCaseNote.subType}",
                "eventId" to prisonCaseNote.eventId.toString(),
                "created" to DeliusDateTimeFormatter.format(prisonCaseNote.creationDateTime),
                "occurrence" to DeliusDateTimeFormatter.format(prisonCaseNote.occurrenceDateTime)
            )
        )

        deliusService.mergeCaseNote(prisonCaseNote.toDeliusCaseNote())
    }
}
