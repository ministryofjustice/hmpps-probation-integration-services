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
import java.time.ZonedDateTime

@Service
class DomainEventService(
    private val objectMapper: ObjectMapper,
    private val referenceDataRepository: ReferenceDataRepository,
    private val domainEventRepository: DomainEventRepository
) {

    fun publishContactUpdated(
        crn: String,
        contactId: Long,
        export: Boolean,
        category: Int,
        occurredAt: ZonedDateTime
    ) {
        publishMappaEvent(
            eventType = MappaDomainEventType.UPDATED,
            description = "MAPPS information has been updated in NDelius",
            crn = crn,
            contactId = contactId,
            export = export,
            category = category,
            occurredAt = occurredAt,
            includePreviousCrn = false
        )
    }

    fun publishContactDeleted(
        crn: String,
        contactId: Long,
        export: Boolean,
        category: Int,
        occurredAt: ZonedDateTime
    ) {
        publishMappaEvent(
            eventType = MappaDomainEventType.DELETED,
            description = "MAPPS information has been deleted in NDelius",
            crn = crn,
            contactId = contactId,
            export = export,
            category = category,
            occurredAt = occurredAt,
            includePreviousCrn = true
        )
    }

    private fun publishMappaEvent(
        eventType: String,
        description: String,
        crn: String,
        contactId: Long,
        export: Boolean,
        category: Int,
        occurredAt: ZonedDateTime,
        includePreviousCrn: Boolean
    ) {
        val additionalInformation = mutableMapOf<String, Any?>(
            "contactId" to contactId,
            "mapps" to mapOf(
                "export" to export,
                "category" to category
            )
        )

        if (includePreviousCrn) {
            additionalInformation["previousCrn"] = crn
        }

        val event = HmppsDomainEvent(
            eventType = eventType,
            version = 1,
            description = description,
            occurredAt = occurredAt,
            personReference = personReferenceForCrn(crn),
            additionalInformation = additionalInformation
        )

        domainEventRepository.save(
            DomainEvent(
                type = referenceDataRepository.domainEventType(eventType),
                messageBody = objectMapper.writeValueAsString(event),
                messageAttributes = objectMapper.writeValueAsString(
                    MessageAttributes(eventType)
                )
            )
        )
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
