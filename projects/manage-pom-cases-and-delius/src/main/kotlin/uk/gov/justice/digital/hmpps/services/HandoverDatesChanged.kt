package uk.gov.justice.digital.hmpps.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.managepomcases.Handover
import uk.gov.justice.digital.hmpps.integrations.managepomcases.ManagePomCasesClient
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.net.URI
import java.time.format.DateTimeFormatter

@Service
class HandoverDatesChanged(
    private val pomCasesClient: ManagePomCasesClient,
    private val personRepository: PersonRepository,
    private val keyDateService: KeyDateService,
    private val telemetryService: TelemetryService
) {
    fun process(event: HmppsDomainEvent) {
        val handOver = event.detailUrl?.let { pomCasesClient.getDetails(URI.create(it)) }
            ?: throw IllegalStateException("No handover data available from ${event.detailUrl}")
        val personId = personRepository.findIdFromNomsId(handOver.nomsId)
            ?: throw NotFoundException("Person", "nomsId", handOver.nomsId)

        val result = keyDateService.mergeHandoverDates(personId, handOver.date, handOver.startDate)
        telemetryService.trackEvent(result.name, handOver.properties())
    }

    fun Handover.properties() = listOfNotNull(
        "nomsId" to nomsId,
        "handoverDate" to date.format(DateTimeFormatter.ISO_LOCAL_DATE),
        startDate?.let { "handoverStartDate" to it.format(DateTimeFormatter.ISO_LOCAL_DATE) }
    ).toMap()
}
