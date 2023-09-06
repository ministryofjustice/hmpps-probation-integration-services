package uk.gov.justice.digital.hmpps.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.managepomcases.ManagePomCasesClient
import uk.gov.justice.digital.hmpps.integrations.managepomcases.PomAllocation
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.IgnorableMessageException
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.net.URI
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Service
class PomAllocated(
    private val pomCasesClient: ManagePomCasesClient,
    private val personRepository: PersonRepository,
    private val prisonManagerService: PrisonManagerService,
    private val telemetryService: TelemetryService
) {
    fun process(event: HmppsDomainEvent) = try {
        val pomAllocation = event.detailUrl?.let { pomCasesClient.getPomAllocation(URI.create(it)) }
            ?: throw IgnorableMessageException(
                "No POM Allocation data available",
                mapOf("detailUrl" to event.detailUrl.orNotProvided())
            )

        val nomsId = event.personReference.findNomsNumber()

        val personId = nomsId?.let { personRepository.findIdFromNomsId(it) }
            ?: throw IgnorableMessageException("PersonNotFound")

        prisonManagerService.allocatePrisonManager(personId, event.occurredAt, pomAllocation)
        telemetryService.trackEvent("POM Allocated", pomAllocation.properties(nomsId, event.occurredAt))
    } catch (ime: IgnorableMessageException) {
        telemetryService.trackEvent(
            ime.message,
            mapOf("nomsId" to event.personReference.findNomsNumber().orNotProvided()) + ime.additionalInformation
        )
    }

    fun String?.orNotProvided() = this ?: "Not Provided"

    fun PomAllocation.properties(nomsId: String?, allocationDate: ZonedDateTime) = listOfNotNull(
        "prisonId" to prison.code,
        "nomsId" to nomsId.orNotProvided(),
        "allocationDate" to allocationDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
    ).toMap()
}
