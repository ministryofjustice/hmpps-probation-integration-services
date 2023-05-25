package uk.gov.justice.digital.hmpps.controller.personaldetails

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.controller.personaldetails.entity.PersonRepository
import uk.gov.justice.digital.hmpps.controller.personaldetails.entity.getPerson
import uk.gov.justice.digital.hmpps.controller.personaldetails.model.PersonalDetails

@Service
class PersonalDetailsService(val personRepository: PersonRepository, val personMapper: PersonMapper) {
    fun getPersonalDetails(crn: String): PersonalDetails {
        val person = personRepository.getPerson(crn)
        return personMapper.convertToModel(person)
    }
}
