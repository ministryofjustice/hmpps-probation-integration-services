package uk.gov.justice.digital.hmpps.listener

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.digital.hmpps.integrations.delius.service.DeliusService
import uk.gov.justice.digital.hmpps.integrations.nomis.CaseNoteMessage
import uk.gov.justice.digital.hmpps.integrations.nomis.NomisClient
import uk.gov.justice.digital.hmpps.integrations.nomis.toDeliusCaseNote


@Component
class MessageListener(
    val nc: NomisClient,
    val ds: DeliusService
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @JmsListener(destination = "events")
    fun receive(caseNoteMessage: CaseNoteMessage) {
        val nomisCaseNote = try {
            nc.getCaseNote(caseNoteMessage.offenderId, caseNoteMessage.caseNoteId)
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

        ds.mergeCaseNote(nomisCaseNote.toDeliusCaseNote())
    }
}