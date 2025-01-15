package uk.gov.justice.digital.hmpps.messaging

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpStatusCodeException
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.exceptions.OffenderNotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.service.DeliusService
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNote
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNoteFilters
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNotesClient
import uk.gov.justice.digital.hmpps.integrations.prison.toDeliusCaseNote
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.net.URI

@Service
class CaseNotePublished(
    private val prisonCaseNotesClient: PrisonCaseNotesClient,
    private val deliusService: DeliusService,
    private val telemetryService: TelemetryService,
) {
    fun handle(event: HmppsDomainEvent) {
        val caseNoteId = event.additionalInformation["caseNoteId"]
        if (caseNoteId == null) {
            telemetryService.trackEvent(
                "MissingCaseNoteId",
                mapOf(
                    "eventType" to event.eventType,
                    "nomsNumber" to event.personReference.findNomsNumber()!!
                )
            )
            return
        }

        val prisonCaseNote = try {
            prisonCaseNotesClient.getCaseNote(URI.create(event.detailUrl!!))
        } catch (ex: HttpStatusCodeException) {
            when (ex.statusCode) {
                HttpStatus.NOT_FOUND -> {
                    telemetryService.trackEvent("CaseNoteNotFound", mapOf("detailUrl" to event.detailUrl!!))
                    return
                }

                else -> throw ex
            }
        }

        val reasonToIgnore: Lazy<String?> = lazy {
            PrisonCaseNoteFilters.filters.firstOrNull { it.predicate.invoke(prisonCaseNote!!) }?.reason
        }

        if (prisonCaseNote == null || reasonToIgnore.value != null) {
            val reason = if (prisonCaseNote == null) {
                "case note was not able to be retrieved"
            } else {
                reasonToIgnore.value!!
            }
            telemetryService.trackEvent(
                "CaseNoteIgnored",
                (prisonCaseNote?.properties() ?: emptyMap()) + ("reason" to reason)
            )
            return
        }

        try {
            deliusService.mergeCaseNote(prisonCaseNote.toDeliusCaseNote(event.occurredAt))
            telemetryService.trackEvent("CaseNoteMerged", prisonCaseNote.properties())
        } catch (e: Exception) {
            telemetryService.trackEvent(
                "CaseNoteMergeFailed",
                prisonCaseNote.properties() + ("exception" to (e.message ?: ""))
            )
            if (e !is OffenderNotFoundException) throw e
        }
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