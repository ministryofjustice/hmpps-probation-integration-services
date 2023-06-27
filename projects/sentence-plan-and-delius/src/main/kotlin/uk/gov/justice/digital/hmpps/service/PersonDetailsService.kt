package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.model.CaseDetails
import uk.gov.justice.digital.hmpps.model.Manager
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.service.entity.EventRepository
import uk.gov.justice.digital.hmpps.service.entity.PersonRepository
import uk.gov.justice.digital.hmpps.service.entity.getPerson
import uk.gov.justice.digital.hmpps.service.entity.isInCustody
import uk.gov.justice.digital.hmpps.service.entity.name

@Service
class PersonDetailsService(val personRepository: PersonRepository, val eventRepository: EventRepository) {
    fun getPersonalDetails(crn: String): CaseDetails {
        val personEntity = personRepository.getPerson(crn)
        val manager = personEntity.manager
        return CaseDetails(
            personEntity.name(),
            personEntity.crn,
            personEntity.tier?.description,
            personEntity.dateOfBirth,
            personEntity.nomisId,
            manager.team.probationArea.description,
            Manager(
                Name(manager.staff.forename, manager.staff.middleName, manager.staff.surname),
                manager.staff.isUnallocated()
            ),
            eventRepository.isInCustody(crn)
        )
    }
}
