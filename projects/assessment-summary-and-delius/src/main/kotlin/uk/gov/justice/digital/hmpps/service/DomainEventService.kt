package uk.gov.justice.digital.hmpps.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.domainevent.entity.DomainEvent
import uk.gov.justice.digital.hmpps.integrations.delius.domainevent.entity.DomainEventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Registration
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
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
    fun publishEvents(hmppsDomainEvents: List<HmppsDomainEvent>) = domainEventRepository.saveAll(
        hmppsDomainEvents.map {
            DomainEvent(
                referenceDataRepository.domainEventType(it.eventType),
                objectMapper.writeValueAsString(it),
                objectMapper.writeValueAsString(MessageAttributes(it.eventType))
            )
        }
    )
}

fun Registration.deRegEvent(crn: String): HmppsDomainEvent = HmppsDomainEvent(
    eventType = ReferenceData.Code.REGISTRATION_DEREGISTERED.value,
    version = 1,
    occurredAt = ZonedDateTime.now(),
    personReference = forCrn(crn),
    additionalInformation = mapOf(
        "registerTypeCode" to type.code,
        "registerTypeDescription" to type.description,
        "deregistrationId" to deregistration!!.id,
        "deregistrationDate" to deregistration!!.date,
        "createdDateAndTime" to deregistration!!.createdDatetime
    )
)

fun Registration.regEvent(crn: String): HmppsDomainEvent = HmppsDomainEvent(
    eventType = ReferenceData.Code.REGISTRATION_ADDED.value,
    version = 1,
    personReference = forCrn(crn),
    additionalInformation = mapOf(
        "registrationId" to id,
        "registerTypeCode" to type.code,
        "registerTypeDescription" to type.description,
        "registrationDate" to date,
        "createdDateAndTime" to createdDatetime
    )
)

fun forCrn(crn: String) = PersonReference(listOf(PersonIdentifier("CRN", crn)))
