package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.detail.DomainEventDetailService
import uk.gov.justice.digital.hmpps.exceptions.OffenderNotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.model.properties
import uk.gov.justice.digital.hmpps.integrations.delius.service.DeliusService
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNote
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNoteFilters.filters
import uk.gov.justice.digital.hmpps.integrations.prison.toDeliusCaseNote
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Service
class PersonCaseNote(
    private val detailService: DomainEventDetailService,
    private val deliusService: DeliusService,
    private val telemetryService: TelemetryService,
) {
    fun handle(event: HmppsDomainEvent) {
        val prisonCaseNote: PrisonCaseNote = try {
            detailService.getDetail(event)
        } catch (_: HttpClientErrorException.NotFound) {
            return telemetryService.trackEvent("CaseNoteNotFound", mapOf("detailUrl" to event.detailUrl!!))
        }

        filters.firstOrNull { it.predicate.invoke(prisonCaseNote) }?.reason?.also {
            telemetryService.trackEvent(
                "CaseNoteIgnored",
                (prisonCaseNote.properties()) + ("reason" to it)
            )
            return
        }

        try {
            val result = deliusService.mergeCaseNote(prisonCaseNote.toDeliusCaseNote())
            telemetryService.trackEvent("CaseNoteMerged", prisonCaseNote.properties() + result.properties())
        } catch (e: Exception) {
            telemetryService.trackEvent(
                "CaseNoteMergeFailed",
                prisonCaseNote.properties() + ("exception" to (e.message ?: ""))
            )
            if (e !is OffenderNotFoundException) throw e
        }
    }

    private fun PrisonCaseNote.properties(): Map<String, String> = mapOf(
        "caseNoteId" to id.toString(),
        "type" to type,
        "subType" to subType,
        "eventId" to eventId.toString(),
        "created" to DeliusDateTimeFormatter.format(creationDateTime),
        "occurrence" to DeliusDateTimeFormatter.format(occurrenceDateTime),
        "location" to locationId
    )
}