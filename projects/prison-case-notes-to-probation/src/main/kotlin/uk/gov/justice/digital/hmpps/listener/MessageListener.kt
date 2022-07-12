package uk.gov.justice.digital.hmpps.listener

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.integrations.delius.service.DeliusService
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNotesClient
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonOffenderEvent
import uk.gov.justice.digital.hmpps.integrations.prison.toDeliusCaseNote
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import javax.jms.TextMessage

@Component
@EnableJms
class MessageListener(
    val prisonCaseNotesClient: PrisonCaseNotesClient,
    val deliusService: DeliusService,
    val telemetryService: TelemetryService,
    val jmsTemplate: JmsTemplate,
    @Value("\${integrations.prison-offender-events.queue}") val queueName: String
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @JmsListener(destination = "\${integrations.prison-offender-events.dlq}")
    @ConditionalOnProperty("integrations.prison-offender-events.retry-dlq-messages", havingValue = "true", matchIfMissing = true)
    fun retry(message: TextMessage) {
        telemetryService.trackEvent(
            "RetryDLQMessage",
            mapOf(
                "messageID" to message.jmsMessageID,
                "correlationID" to message.jmsCorrelationID,
                "text" to message.text
            )
        )
        jmsTemplate.convertAndSend(queueName, message.text)
    }

    @JmsListener(destination = "\${integrations.prison-offender-events.queue}")
    fun receive(prisonOffenderEvent: PrisonOffenderEvent) {
        if (prisonOffenderEvent.caseNoteId == null) {
            log.info("Received ${prisonOffenderEvent.eventType} for ${prisonOffenderEvent.offenderId} without a case note id")
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
