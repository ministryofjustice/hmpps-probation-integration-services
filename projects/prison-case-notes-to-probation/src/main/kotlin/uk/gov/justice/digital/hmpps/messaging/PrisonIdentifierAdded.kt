package uk.gov.justice.digital.hmpps.messaging

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.integrations.delius.service.DeliusService
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNoteFilters
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNotesClient
import uk.gov.justice.digital.hmpps.integrations.prison.SearchCaseNotes
import uk.gov.justice.digital.hmpps.integrations.prison.SearchCaseNotes.Companion.TYPES_OF_INTEREST
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
    @Transactional
    fun handle(event: HmppsDomainEvent) {
        val nomsId = checkNotNull(event.personReference.findNomsNumber()) {
            "NomsNumber not found for ${event.eventType}"
        }
        val uri = URI.create("$caseNotesBaseUrl/search/case-notes/$nomsId")
        val caseNotes = caseNotesApi.searchCaseNotes(uri, SearchCaseNotes(TYPES_OF_INTEREST)).content
            .filter { cn -> PrisonCaseNoteFilters.filters.none { it.predicate.invoke(cn) } }

        caseNotes.forEach { deliusService.mergeCaseNote(it.toDeliusCaseNote()) }

        telemetryService.trackEvent(
            "CaseNotesMigrated", mapOf(
                "nomsId" to nomsId,
                "cause" to event.eventType,
                "count" to caseNotes.size.toString()
            )
        )
    }
}