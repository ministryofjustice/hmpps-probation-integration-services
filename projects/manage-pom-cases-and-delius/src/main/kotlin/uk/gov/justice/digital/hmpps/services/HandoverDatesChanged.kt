package uk.gov.justice.digital.hmpps.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.managepomcases.Handover
import uk.gov.justice.digital.hmpps.integrations.managepomcases.ManagePomCasesClient
import uk.gov.justice.digital.hmpps.messaging.HandoverMessage
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.net.URI

@Service
class HandoverDatesChanged(
    private val pomCasesClient: ManagePomCasesClient,
    private val personRepository: PersonRepository,
    private val keyDateService: KeyDateService,
    private val telemetryService: TelemetryService
) {
    fun process(event: HandoverMessage, dryRun: Boolean = false) = try {
        val handOver = event.detailUrl?.let { pomCasesClient.getDetails(URI.create(it)) }
            ?: throw IgnorableMessageException(
                "No handover data available",
                mapOf("detailUrl" to event.detailUrl.orNotProvided())
            )
        val personId = personRepository.findIdFromNomsId(handOver.nomsId)
            ?: throw IgnorableMessageException("PersonNotFound", handOver.properties())

        val result = keyDateService.mergeHandoverDates(personId, handOver.date, handOver.startDate, dryRun)
        telemetryService.trackEvent(result.name, handOver.properties())
    } catch (ime: IgnorableMessageException) {
        telemetryService.trackEvent(
            ime.message,
            mapOf("nomsId" to event.personReference.findNomsNumber().orNotProvided()) + ime.additionalProperties
        )
    }

    fun String?.orNotProvided() = this ?: "Not Provided"

    fun Handover.properties() = listOfNotNull(
        "nomsId" to nomsId,
        "handoverDate" to date.toString(),
        "handoverStartDate" to startDate.toString()
    ).toMap()
}
