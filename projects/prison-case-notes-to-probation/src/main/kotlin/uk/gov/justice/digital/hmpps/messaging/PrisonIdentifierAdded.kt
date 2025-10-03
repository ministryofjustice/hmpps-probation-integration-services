package uk.gov.justice.digital.hmpps.messaging

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exceptions.OffenderNotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.model.MergeResult
import uk.gov.justice.digital.hmpps.integrations.delius.model.MergeResult.Action.Created
import uk.gov.justice.digital.hmpps.integrations.delius.model.MergeResult.Action.Updated
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
        }.uppercase()
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

        val results: List<MergeResult> = cnResults + alertResults
        val success = results.firstOrNull { it is Success } as Success?

        val cnResultMap = cnResults.filterIsInstance<Success>().groupBy { it.action.name }
        val alertResultMap = alertResults.filterIsInstance<Success>().groupBy { it.action.name }

        try {
            val moved = results
                .filterIsInstance<Success>().map { it.action }
                .filterIsInstance<MergeResult.Action.Moved>()
                .map { it.from }.toSet()
            deliusService.createDataCleanseContact(moved)
        } catch (e: Exception) {
            telemetryService.trackEvent("DataCleanseNotificationFailure", mapOf("reason" to e.message))
        }

        telemetryService.trackEvent(
            "CaseNotesMigrated", listOfNotNull(
                "nomsId" to nomsId,
                success?.crn?.let { "crn" to it },
                "cause" to event.eventType,
                cnResultMap[Created.name]?.let { "caseNotesCreated" to it.size.toString() },
                cnResultMap[Updated.name]?.let { "caseNotesUpdated" to it.size.toString() },
                cnResultMap["Moved"]?.let { "caseNotesMoved" to it.size.toString() },
                alertResultMap[Created.name]?.let { "alertsCreated" to it.size.toString() },
                alertResultMap[Updated.name]?.let { "alertsUpdated" to it.size.toString() },
                alertResultMap["Moved"]?.let { "alertsMoved" to it.size.toString() },
            ).toMap()
        )

        results.filterIsInstance<Failure>().firstOrNull{ it.exception !is OffenderNotFoundException }?.let { throw it.exception }
    }
}