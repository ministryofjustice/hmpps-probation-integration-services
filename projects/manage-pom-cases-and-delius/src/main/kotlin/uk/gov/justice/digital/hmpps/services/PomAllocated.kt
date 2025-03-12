package uk.gov.justice.digital.hmpps.services

import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.detail.DomainEventDetailService
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.managepomcases.PomAllocation
import uk.gov.justice.digital.hmpps.integrations.managepomcases.PomDeallocated
import uk.gov.justice.digital.hmpps.integrations.managepomcases.PomNotAllocated
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@Service
class PomAllocated(
    private val detailService: DomainEventDetailService,
    private val personRepository: PersonRepository,
    private val prisonManagerService: PrisonManagerService,
    private val telemetryService: TelemetryService
) {
    fun process(event: HmppsDomainEvent) = try {
        val pomAllocation = try {
            detailService.getDetail<PomAllocation>(event)
        } catch (e: HttpClientErrorException.NotFound) {
            val error = e.getResponseBodyAs(ErrorResponse::class.java)
            when (error?.message) {
                "Not allocated" -> PomDeallocated
                else -> PomNotAllocated
            }
        }

        val nomsId = event.personReference.findNomsNumber()
        val personId = nomsId?.let { personRepository.findIdFromNomsId(it) }
            ?: throw IgnorableMessageException("PersonNotFound")

        when (pomAllocation) {
            is PomAllocation -> {
                val res = prisonManagerService.allocatePrisonManager(personId, event.occurredAt, pomAllocation)
                telemetryService.trackEvent(res.name, pomAllocation.properties(nomsId, event.occurredAt))
            }

            is PomDeallocated -> {
                val res = prisonManagerService.deallocatePrisonManager(personId, event.occurredAt)
                telemetryService.trackEvent(res.name, event.properties())
            }

            else -> {
                telemetryService.trackEvent("NotReadyToAllocate", event.properties())
            }
        }
    } catch (ime: IgnorableMessageException) {
        telemetryService.trackEvent(
            ime.message,
            mapOf("nomsId" to event.personReference.findNomsNumber().orNotProvided()) + ime.additionalProperties
        )
    }

    fun String?.orNotProvided() = this ?: "Not Provided"

    fun PomAllocation.properties(nomsId: String?, allocationDate: ZonedDateTime) = listOfNotNull(
        "prisonId" to prison.code,
        "nomsId" to nomsId.orNotProvided(),
        "allocationDate" to DeliusDateTimeFormatter.format(allocationDate)
    ).toMap()

    fun HmppsDomainEvent.properties() = mapOf(
        "nomsId" to personReference.findNomsNumber().orNotProvided(),
        "allocationDate" to DeliusDateTimeFormatter.format(occurredAt)
    )
}

data class ErrorResponse(val message: String?)
