package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.model.CaseDetails
import uk.gov.justice.digital.hmpps.model.Manager
import uk.gov.justice.digital.hmpps.model.Name

@Service
class PersonDetailsService(val personRepository: PersonRepository) {
    fun getPersonalDetails(crn: String): CaseDetails {
        val personEntity = personRepository.getPerson(crn)
        val manager = personEntity.manager
        return CaseDetails(
            personEntity.name(),
            personEntity.crn,
            personEntity.tier?.code,
            personEntity.dateOfBirth,
            personEntity.nomisId,
            manager.team.probationArea.description,
            Manager(
                Name(manager.staff.forename, manager.staff.middleName, manager.staff.surname),
                manager.staff.isUnallocated()
            )
        )
    }
}
