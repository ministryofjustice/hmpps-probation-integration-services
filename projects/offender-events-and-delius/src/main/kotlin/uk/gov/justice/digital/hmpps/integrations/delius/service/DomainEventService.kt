package uk.gov.justice.digital.hmpps.integrations.delius.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.domainevent.entity.DomainEvent
import uk.gov.justice.digital.hmpps.integrations.delius.domainevent.entity.DomainEventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.domainEventType
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.PersonIdentifier
import uk.gov.justice.digital.hmpps.message.PersonReference
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@Service
class DomainEventService(
    private val objectMapper: ObjectMapper,
    private val referenceDataRepository: ReferenceDataRepository,
    private val domainEventRepository: DomainEventRepository,
    private val telemetryService: TelemetryService,
) {

    fun publishContactUpdated(
        crn: String,
        contactId: Long,
        category: Int,
        occurredAt: ZonedDateTime
    ) {
        publishDomainEvent(
            HmppsDomainEvent(
                eventType = MappaDomainEventType.UPDATED,
                version = 1,
                description = "MAPPA information has been updated in NDelius",
                occurredAt = occurredAt,
                personReference = personReferenceForCrn(crn),
                additionalInformation = mapOf(
                    "contactId" to contactId,
                    "mappa" to mapOf(
                        "category" to category
                    )
                )
            )
        )
    }

    fun publishContactDeleted(
        crn: String,
        contactId: Long,
        category: Int,
        occurredAt: ZonedDateTime
    ) {
        publishDomainEvent(
            HmppsDomainEvent(
                eventType = MappaDomainEventType.DELETED,
                version = 1,
                description = "MAPPA information has been deleted in NDelius",
                occurredAt = occurredAt,
                personReference = personReferenceForCrn(crn),
                additionalInformation = mapOf(
                    "contactId" to contactId,
                    "mappa" to mapOf(
                        "category" to category
                    )
                )
            )
        )
    }

    private fun publishDomainEvent(event: HmppsDomainEvent) {
        domainEventRepository.save(
            DomainEvent(
                type = referenceDataRepository.domainEventType(event.eventType),
                messageBody = objectMapper.writeValueAsString(event),
                messageAttributes = objectMapper.writeValueAsString(
                    MessageAttributes(event.eventType)
                )
            )
        ).also { domainEvent ->
            telemetryService.trackEvent(
                "DomainEventSaved",
                mapOf(
                    "domainEventId" to domainEvent.id.toString(),
                    "eventType" to event.eventType,
                    "crn" to event.personReference.findCrn(),
                    "nomsNumber" to event.personReference.findNomsNumber(),
                    "occurredAt" to event.occurredAt.toString()
                ).filterValues { it != null }
            )
        }
    }

    private fun personReferenceForCrn(crn: String): PersonReference =
        PersonReference(
            listOf(PersonIdentifier("CRN", crn))
        )
}

private object MappaDomainEventType {
    const val UPDATED = "probation-case.mappa-information.updated"
    const val DELETED = "probation-case.mappa-information.deleted"
}
