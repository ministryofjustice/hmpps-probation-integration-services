package uk.gov.justice.digital.hmpps.messaging

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
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

        val cnExceptions = caseNotes.mapNotNull { pcn ->
            try {
                deliusService.mergeCaseNote(pcn.toDeliusCaseNote())
                null
            } catch (e: Exception) {
                e
            }
        }

        val alertExceptions = alerts.mapNotNull { alert ->
            try {
                deliusService.mergeCaseNote(alert.toDeliusCaseNote())
                null
            } catch (e: Exception) {
                e
            }
        }

        (cnExceptions + alertExceptions).firstOrNull()?.also { throw it }

        telemetryService.trackEvent(
            "CaseNotesMigrated", mapOf(
                "nomsId" to nomsId,
                "cause" to event.eventType,
                "caseNotes" to caseNotes.size.toString(),
                "alerts" to alerts.size.toString(),
            )
        )
    }
}