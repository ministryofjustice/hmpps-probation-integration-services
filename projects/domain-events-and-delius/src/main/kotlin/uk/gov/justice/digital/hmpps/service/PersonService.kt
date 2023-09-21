package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import java.time.LocalDate

@Service
class PersonService(private val personRepository: PersonRepository) {
    fun findEngagement(crn: String): Engagement = personRepository.findByCrn(crn)?.asEngagement()
        ?: throw NotFoundException("Engagement", "crn", crn)
}

private fun Person.asEngagement() = Engagement(
    Identifiers(crn, pnc),
    Name(forename, surname, listOfNotNull(secondName, thirdName)),
    dateOfBirth
)

data class Engagement(
    val identifiers: Identifiers,
    val name: Name,
    val dateOfBirth: LocalDate
)

data class Identifiers(val crn: String, val pnc: String?)

data class Name(
    val forename: String,
    val surname: String,
    val otherNames: List<String>
)