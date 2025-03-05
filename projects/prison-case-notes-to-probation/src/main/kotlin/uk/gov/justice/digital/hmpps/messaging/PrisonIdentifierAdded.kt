package uk.gov.justice.digital.hmpps.messaging

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.delius.model.MergeResult
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
    private val telemetryService: TelemetryService,
    private val featureFlags: FeatureFlags
) {
    fun handle(event: HmppsDomainEvent) {
        val useAlertsApi = featureFlags.enabled("alert-case-notes-from-alerts-api")

        val nomsId = checkNotNull(event.personReference.findNomsNumber()) {
            "NomsNumber not found for ${event.eventType}"
        }
        val uri = URI.create("$caseNotesBaseUrl/search/case-notes/$nomsId")
        val caseNotes = caseNotesApi.searchCaseNotes(uri, SearchCaseNotes(forSearchRequest(useAlertsApi))).content
            .filter { cn -> PrisonCaseNoteFilters.filters.none { it.predicate.invoke(cn) } }

        val alerts = if (useAlertsApi) {
            alertsApi.getActiveAlerts(URI.create("$alertsBaseUrl/prisoners/$nomsId/alerts")).content
        } else {
            emptyList()
        }

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

        telemetryService.trackEvent(
            "CaseNotesMigrated", listOfNotNull(
                "nomsId" to nomsId,
                success?.crn?.let { "crn" to it },
                "cause" to event.eventType,
                "caseNotes" to cnResults.filterIsInstance<Success>().size.toString(),
                "alerts" to alertResults.filterIsInstance<Success>().size.toString(),
            ).toMap()
        )

        results.filterIsInstance<Failure>().firstOrNull()?.let { throw it.exception }
    }
}