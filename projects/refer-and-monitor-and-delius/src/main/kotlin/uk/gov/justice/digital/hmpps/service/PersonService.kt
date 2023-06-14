package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.CaseIdentifier
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository

@Service
class PersonService(private val personRepository: PersonRepository) {
    fun findIdentifiers(crn: String) = CaseIdentifier(crn, personRepository.findNomsId(crn))
}
