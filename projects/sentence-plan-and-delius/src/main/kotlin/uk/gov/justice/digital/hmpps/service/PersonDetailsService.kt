package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.model.Manager
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.model.PersonDetails

@Service
class PersonDetailsService(val personRepository: PersonRepository) {
    fun getPersonalDetails(crn: String): PersonDetails {
        val personEntity = personRepository.getPerson(crn)
        val manager = personEntity.managers[0]
        return PersonDetails(
            personEntity.name(),
            personEntity.crn,
            personEntity.tier?.code,
            personEntity.dateOfBirth,
            personEntity.nomisId,
            manager.team.probationArea.description,
            Manager(
                Name(manager.staff.forename, manager.staff.middleName, manager.staff.surname),
                manager.staff.isUnallocated()
            ),
        )
    }
}