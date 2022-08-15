package uk.gov.justice.digital.hmpps.listener

import feign.FeignException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.integrations.delius.service.DeliusService
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNote
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNoteFilters
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

        val reasonToIgnore: Lazy<String?> = lazy {
            PrisonCaseNoteFilters.filters.firstOrNull { it.predicate.invoke(prisonCaseNote!!) }?.reason
        }

        if (prisonCaseNote?.text == null || reasonToIgnore.value != null) {
            val reason = if (prisonCaseNote == null) "case note was not found" else {
                telemetryService.trackEvent(
                    "CaseNoteIgnored",
                    prisonCaseNote.properties()
                )
                reasonToIgnore.value
            }
            log.warn(
                "Ignoring case note id {} and type {} because $reason",
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
            prisonCaseNote.properties()
        )

        deliusService.mergeCaseNote(prisonCaseNote.toDeliusCaseNote())
    }

    private fun PrisonCaseNote.properties() = mapOf(
        "caseNoteId" to id,
        "type" to type,
        "subType" to subType,
        "eventId" to eventId.toString(),
        "created" to DeliusDateTimeFormatter.format(creationDateTime),
        "occurrence" to DeliusDateTimeFormatter.format(occurrenceDateTime),
        "location" to locationId
    )
}
