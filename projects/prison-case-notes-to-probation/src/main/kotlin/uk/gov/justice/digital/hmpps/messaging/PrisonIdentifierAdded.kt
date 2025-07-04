package uk.gov.justice.digital.hmpps.messaging

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.model.MergeResult
import uk.gov.justice.digital.hmpps.integrations.delius.model.MergeResult.Action.Created
import uk.gov.justice.digital.hmpps.integrations.delius.model.MergeResult.Failure
import uk.gov.justice.digital.hmpps.integrations.delius.model.MergeResult.Success
import uk.gov.justice.digital.hmpps.integrations.delius.service.DeliusService
import uk.gov.justice.digital.hmpps.integrations.prison.*
import uk.gov.justice.digital.hmpps.integrations.prison.CaseNoteTypesOfInterest.forSearchRequest
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.net.URI

@Service
class PrisonIdentifierAdded(
    private val caseNotesApi: PrisonCaseNotesClient,
    private val deliusService: DeliusService,
    @Value("\${integrations.prison-case-notes.base_url}")
    private val caseNotesBaseUrl: String,
    @Value("\${integrations.prisoner-alerts.base_url}")
    private val alertsBaseUrl: String,
    private val alertsApi: PrisonerAlertClient,
    private val telemetryService: TelemetryService
) {
    fun handle(event: HmppsDomainEvent) {

        val nomsId = checkNotNull(event.personReference.findNomsNumber()) {
            "NomsNumber not found for ${event.eventType}"
        }
        val uri = URI.create("$caseNotesBaseUrl/search/case-notes/$nomsId")
        val caseNotes = caseNotesApi.searchCaseNotes(uri, SearchCaseNotes(forSearchRequest())).content
            .filter { cn -> PrisonCaseNoteFilters.filters.none { it.predicate.invoke(cn) } }

        val alerts = alertsApi.getActiveAlerts(URI.create("$alertsBaseUrl/prisoners/$nomsId/alerts")).content
            .filter { it.alertCode.code != AlertCode.OCG_NOMINAL }

        val cnResults: List<MergeResult> = caseNotes.mapNotNull {
            try {
                deliusService.mergeCaseNote(it.toDeliusCaseNote())
            } catch (e: Exception) {
                Failure(e)
            }
        }

        val alertResults: List<MergeResult> = alerts.mapNotNull {
            try {
                deliusService.mergeCaseNote(it.toDeliusCaseNote())
            } catch (e: Exception) {
                Failure(e)
            }
        }

        val results = cnResults + alertResults
        val success = results.firstOrNull { it is Success } as Success?

        val (cnCreated, cnUpdated) = cnResults.filterIsInstance<Success>().partition { it.action == Created }
        val (alertCreated, alertUpdated) = alertResults.filterIsInstance<Success>().partition { it.action == Created }

        telemetryService.trackEvent(
            "CaseNotesMigrated", listOfNotNull(
                "nomsId" to nomsId,
                success?.crn?.let { "crn" to it },
                "cause" to event.eventType,
                "caseNotesCreated" to cnCreated.size.toString(),
                "caseNotesUpdated" to cnUpdated.size.toString(),
                "alertsCreated" to alertCreated.size.toString(),
                "alertsUpdated" to alertUpdated.size.toString()
            ).toMap()
        )

        results.filterIsInstance<Failure>().firstOrNull()?.let { throw it.exception }
    }
}