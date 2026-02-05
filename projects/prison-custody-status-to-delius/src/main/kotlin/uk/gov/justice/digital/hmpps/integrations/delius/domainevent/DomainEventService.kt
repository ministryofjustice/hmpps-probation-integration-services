package uk.gov.justice.digital.hmpps.integrations.delius.domainevent

import tools.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.domainevent.entity.DomainEvent
import uk.gov.justice.digital.hmpps.integrations.delius.domainevent.entity.DomainEventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.entity.LicenceCondition
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.domainEventType
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.PersonIdentifier
import uk.gov.justice.digital.hmpps.message.PersonReference

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

    fun publishLicenceConditionTermination(licenceCondition: LicenceCondition) = publish(
        HmppsDomainEvent(
            eventType = "probation-case.licence-condition.terminated",
            description = "A licence condition has been terminated",
            version = 1,
            personReference = forCrn(licenceCondition.disposal.event.person.crn),
            additionalInformation = mapOf(
                "eventNumber" to licenceCondition.disposal.event.number,
                "licconditionId" to licenceCondition.id.toString(),
                "licconditionMainType" to licenceCondition.mainCategory.description,
                "licconditionSubType" to licenceCondition.subCategory?.description,
            )
        )
    )

    private fun forCrn(crn: String) = PersonReference(listOf(PersonIdentifier("CRN", crn)))
}