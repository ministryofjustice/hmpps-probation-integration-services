package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.model.PersonResponse
import uk.gov.justice.digital.hmpps.repository.PersonRepository
import uk.gov.justice.digital.hmpps.repository.getPersonByCrn
import uk.gov.justice.digital.hmpps.repository.getPersonByNoms

@Service
class PersonService(
    private val personRepository: PersonRepository,
) {
    companion object {
        private const val CRN_REGEX = "[A-Za-z][0-9]{6}"
        private const val NOMS_REGEX = "[A-Za-z][0-9]{4}[A-Za-z]{2}"
    }

    fun findPerson(identifier: String): PersonResponse {
        val person = CRN_REGEX.toRegex().find(identifier)?.let { crn -> personRepository.getPersonByCrn(crn.value) }
            ?: NOMS_REGEX.toRegex().find(identifier)
                ?.let { nomsNumber -> personRepository.getPersonByNoms(nomsNumber.value) }
            ?: throw IllegalArgumentException("${identifier} is not a valid crn or prisoner number")
        return PersonResponse(
            crn = person.crn,
            name = Name(
                person.forename,
                listOfNotNull(person.secondName, person.thirdName).joinToString(" "),
                person.surname
            ),
            nomsNumber = person.nomsNumber,
            dateOfBirth = person.dateOfBirth,
            ethnicity = person.ethnicity?.description,
            gender = person.gender.description,
        )
    }
}

fun Person.toPersonResponse() = PersonResponse(
    crn = crn,
    name = Name(forename, listOfNotNull(secondName, thirdName).joinToString(" "), surname),
    nomsNumber = nomsNumber,
    dateOfBirth = dateOfBirth,
    ethnicity = ethnicity?.description,
    gender = gender.description,
)
