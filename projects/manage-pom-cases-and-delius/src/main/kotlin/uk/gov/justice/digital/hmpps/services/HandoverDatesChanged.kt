package uk.gov.justice.digital.hmpps.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.detail.DomainEventDetailService
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException.Companion.orIgnore
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.keydate.KeyDate
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.managepomcases.Handover
import uk.gov.justice.digital.hmpps.message.PersonReference
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Service
class HandoverDatesChanged(
    private val detailService: DomainEventDetailService,
    private val personRepository: PersonRepository,
    private val keyDateService: KeyDateService,
    private val telemetryService: TelemetryService
) {
    fun process(personReference: PersonReference, detailUrl: String?, dryRun: Boolean = false) = try {
        val handOver = detailService.getDetail<Handover>(detailUrl)
        val personId = personRepository.findIdFromNomsId(handOver.nomsId).orIgnore { "PersonNotFound" }

        val (result, existingDates) = keyDateService
            .mergeHandoverDates(personId, handOver.date, handOver.startDate, dryRun)
        telemetryService.trackEvent(result.name, handOver.properties() + existingDates.properties())
    } catch (ime: IgnorableMessageException) {
        telemetryService.trackEvent(
            ime.message,
            mapOf("nomsId" to personReference.findNomsNumber().orNotProvided()) + ime.additionalProperties
        )
    }

    fun String?.orNotProvided() = this ?: "Not Provided"

    fun Handover.properties() = listOfNotNull(
        "nomsId" to nomsId,
        "handoverDate" to date.toString(),
        "handoverStartDate" to startDate.toString()
    ).toMap()

    private fun Map<String, KeyDate>.properties() =
        mapKeys { "existing${it.key}" }.mapValues { it.value.date.toString() }
}
