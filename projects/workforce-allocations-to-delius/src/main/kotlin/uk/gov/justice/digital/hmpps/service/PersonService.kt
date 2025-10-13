package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Manager
import uk.gov.justice.digital.hmpps.api.model.Person
import uk.gov.justice.digital.hmpps.api.model.ReallocationDetails
import uk.gov.justice.digital.hmpps.api.model.name
import uk.gov.justice.digital.hmpps.api.model.toManager
import uk.gov.justice.digital.hmpps.api.resource.IdentifierType
import uk.gov.justice.digital.hmpps.api.resource.IdentifierType.CRN
import uk.gov.justice.digital.hmpps.api.resource.IdentifierType.NOMS
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.getByCrnAndSoftDeletedFalse
import uk.gov.justice.digital.hmpps.integrations.delius.person.getByNomsIdAndSoftDeletedFalse
import uk.gov.justice.digital.hmpps.integrations.delius.person.getCaseType

@Service
class PersonService(
    private val personRepository: PersonRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val eventRepository: EventRepository,
) {
    fun findByIdentifier(value: String, type: IdentifierType): Person {
        val person = when (type) {
            CRN -> personRepository.getByCrnAndSoftDeletedFalse(value)
            NOMS -> personRepository.getByNomsIdAndSoftDeletedFalse(value)
        }
        val caseType = personRepository.getCaseType(person.crn)
        return Person(person.crn, person.name(), caseType)
    }

    fun reallocationDetails(crn: String): ReallocationDetails {
        val person = personRepository.getByCrnAndSoftDeletedFalse(crn)
        val manager = personManagerRepository.findActiveManager(person.id)
            ?: throw NotFoundException("No manager found for crn")
        val hasActiveOrder = eventRepository.countActiveOrders(person.id) > 0
        return ReallocationDetails(
            person.crn,
            person.name(),
            person.dateOfBirth,
            manager.staff.toManager(manager.team.code),
            hasActiveOrder,
        )
    }
}
