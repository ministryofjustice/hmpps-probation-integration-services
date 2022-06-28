package uk.gov.justice.digital.hmpps.listener

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.digital.hmpps.config.TelemetryService
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.integrations.delius.service.DeliusService
import uk.gov.justice.digital.hmpps.integrations.prison.CaseNoteMessage
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNotesClient
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
    fun receive(caseNoteMessage: CaseNoteMessage) {
        val nomisCaseNote = try {
            prisonCaseNotesClient.getCaseNote(caseNoteMessage.offenderId, caseNoteMessage.caseNoteId)
        } catch (re: ResponseStatusException) {
            log.error("Unable to get Case Note: ${re.rawStatusCode}, ${re.reason}")
            null
        }

        if (nomisCaseNote == null || nomisCaseNote.text.isBlank()) {
            log.warn(
                "Ignoring case note id {} and type {} because case note text is empty",
                caseNoteMessage.caseNoteId,
                caseNoteMessage.eventType
            )
            return
        }

        log.debug(
            "Found case note {} of type {} {} in case notes service, now pushing to delius with event id {}",
            caseNoteMessage.caseNoteId,
            nomisCaseNote.type,
            nomisCaseNote.subType,
            nomisCaseNote.eventId
        )

        telemetryService.trackEvent(
            "CaseNoteMerge",
            mapOf(
                "caseNoteId" to caseNoteMessage.caseNoteId.toString(),
                "type" to "${nomisCaseNote.type}-${nomisCaseNote.subType}",
                "eventId" to nomisCaseNote.eventId.toString(),
                "created" to DeliusDateTimeFormatter.format(nomisCaseNote.creationDateTime),
                "occurrence" to DeliusDateTimeFormatter.format(nomisCaseNote.occurrenceDateTime)
            )
        )

        deliusService.mergeCaseNote(nomisCaseNote.toDeliusCaseNote())
    }
}
