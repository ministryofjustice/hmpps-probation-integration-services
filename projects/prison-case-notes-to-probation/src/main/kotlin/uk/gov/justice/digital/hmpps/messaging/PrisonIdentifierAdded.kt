package uk.gov.justice.digital.hmpps.messaging

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.service.DeliusService
import uk.gov.justice.digital.hmpps.integrations.prison.CaseNoteTypesOfInterest.forSearchRequest
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNoteFilters
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNotesClient
import uk.gov.justice.digital.hmpps.integrations.prison.SearchCaseNotes
import uk.gov.justice.digital.hmpps.integrations.prison.toDeliusCaseNote
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.net.URI

@Service
class PrisonIdentifierAdded(
    private val caseNotesApi: PrisonCaseNotesClient,
    private val deliusService: DeliusService,
    @Value("\${integrations.prison-case-notes.base_url}")
    private val caseNotesBaseUrl: String,
    private val telemetryService: TelemetryService
) {
    fun handle(event: HmppsDomainEvent) {
        val nomsId = checkNotNull(event.personReference.findNomsNumber()) {
            "NomsNumber not found for ${event.eventType}"
        }
        val uri = URI.create("$caseNotesBaseUrl/search/case-notes/$nomsId")
        val caseNotes = caseNotesApi.searchCaseNotes(uri, SearchCaseNotes(forSearchRequest())).content
            .filter { cn -> PrisonCaseNoteFilters.filters.none { it.predicate.invoke(cn) } }

        val exceptions = caseNotes.mapNotNull { pcn ->
            try {
                deliusService.mergeCaseNote(pcn.toDeliusCaseNote())
                null
            } catch (e: Exception) {
                e
            }
        }

        if (exceptions.isNotEmpty()) {
            throw exceptions.first()
        }

        telemetryService.trackEvent(
            "CaseNotesMigrated", mapOf(
                "nomsId" to nomsId,
                "cause" to event.eventType,
                "total" to caseNotes.size.toString(),
            )
        )
    }
}