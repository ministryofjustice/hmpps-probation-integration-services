package uk.gov.justice.digital.hmpps.service

import tools.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.DomainEvent
import uk.gov.justice.digital.hmpps.entity.sentence.component.LicenceCondition
import uk.gov.justice.digital.hmpps.entity.sentence.component.Requirement
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.PersonIdentifier
import uk.gov.justice.digital.hmpps.message.PersonReference
import uk.gov.justice.digital.hmpps.repository.DomainEventRepository
import uk.gov.justice.digital.hmpps.repository.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.repository.domainEventType

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

    fun publishTermination(licenceCondition: LicenceCondition) = publish(
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

    fun publishTermination(requirement: Requirement) = publish(
        HmppsDomainEvent(
            eventType = "probation-case.requirement.terminated",
            description = "A requirement has been terminated",
            version = 1,
            personReference = forCrn(requirement.disposal.event.person.crn),
            additionalInformation = mapOf(
                "eventNumber" to requirement.disposal.event.number,
                "requirementID" to requirement.id.toString(),
                "requirementMainType" to requirement.mainCategory?.description,
                "requirementSubType" to requirement.subCategory?.description,
            )
        )
    )

    private fun forCrn(crn: String) = PersonReference(listOf(PersonIdentifier("CRN", crn)))
}
