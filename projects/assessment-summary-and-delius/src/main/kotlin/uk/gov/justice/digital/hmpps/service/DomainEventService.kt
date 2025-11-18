package uk.gov.justice.digital.hmpps.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.domainevent.entity.DomainEvent
import uk.gov.justice.digital.hmpps.integrations.delius.domainevent.entity.DomainEventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.DeRegistration
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
    fun publish(hmppsDomainEvent: HmppsDomainEvent) = domainEventRepository.save(
        DomainEvent(
            referenceDataRepository.domainEventType(hmppsDomainEvent.eventType),
            objectMapper.writeValueAsString(hmppsDomainEvent),
            objectMapper.writeValueAsString(MessageAttributes(hmppsDomainEvent.eventType))
        )
    )

    fun publishDeregistration(crn: String, deregistration: DeRegistration) = publish(
        HmppsDomainEvent(
            eventType = ReferenceData.Code.REGISTRATION_DEREGISTERED.value,
            description = "A registration no longer applies to the probation case",
            version = 1,
            occurredAt = ZonedDateTime.now(),
            personReference = forCrn(crn),
            additionalInformation = mapOf(
                "registerTypeCode" to deregistration.registration.type.code,
                "registerTypeDescription" to deregistration.registration.type.description,
                "registerLevelCode" to deregistration.registration.level?.code,
                "registerLevelDescription" to deregistration.registration.level?.description,
                "deregistrationId" to deregistration.id,
                "deregistrationDate" to deregistration.date,
                "createdDateAndTime" to deregistration.createdDatetime
            )
        )
    )

    fun publishRegistrationAdded(crn: String, registration: Registration) = publish(
        HmppsDomainEvent(
            eventType = ReferenceData.Code.REGISTRATION_ADDED.value,
            description = "A new registration has been added to the probation case",
            version = 1,
            personReference = forCrn(crn),
            additionalInformation = mapOf(
                "registrationId" to registration.id,
                "registerTypeCode" to registration.type.code,
                "registerTypeDescription" to registration.type.description,
                "registerLevelCode" to registration.level?.code,
                "registerLevelDescription" to registration.level?.description,
                "registrationDate" to registration.date,
                "createdDateAndTime" to registration.createdDatetime
            )
        )
    )

    fun publishRegistrationUpdate(crn: String, registration: Registration) = publish(
        HmppsDomainEvent(
            eventType = ReferenceData.Code.REGISTRATION_UPDATED.value,
            description = "A registration has been updated on the probation case",
            version = 1,
            personReference = forCrn(crn),
            additionalInformation = mapOf(
                "registrationId" to registration.id,
                "registerTypeCode" to registration.type.code,
                "registerTypeDescription" to registration.type.description,
                "registerLevelCode" to registration.level?.code,
                "registerLevelDescription" to registration.level?.description,
                "registrationDate" to registration.date,
                "updatedDateAndTime" to registration.lastUpdatedDatetime
            )
        )
    )

    private fun forCrn(crn: String) = PersonReference(listOf(PersonIdentifier("CRN", crn)))
}
