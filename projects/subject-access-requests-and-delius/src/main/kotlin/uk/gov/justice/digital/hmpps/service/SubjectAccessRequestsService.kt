package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integration.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integration.delius.person.entity.getPerson
import uk.gov.justice.digital.hmpps.integration.delius.person.entity.Person as PersonEntity
import uk.gov.justice.digital.hmpps.api.model.Person

@Service
class SubjectAccessRequestsService(private val personRepository: PersonRepository) {

    fun getPersonDetailsByCrn(crn: String): Person {
        return personRepository.getPerson(crn).toPerson()
    }
}

fun PersonEntity.toPerson(): Person = Person(getName())