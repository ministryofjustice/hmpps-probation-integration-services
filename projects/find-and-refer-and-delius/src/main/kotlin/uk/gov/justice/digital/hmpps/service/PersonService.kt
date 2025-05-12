package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.PersonManager
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.model.PersonResponse
import uk.gov.justice.digital.hmpps.model.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.repository.*

@Service
class PersonService(
    private val personRepository: PersonRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val custodyRepository: CustodyRepository
) {
    companion object {
        private const val CRN_REGEX = "[A-Za-z][0-9]{6}"
        private const val NOMS_REGEX = "[A-Za-z][0-9]{4}[A-Za-z]{2}"
    }

    fun resolveCrn(identifier: String) = resolvePerson(identifier).crn

    fun findPerson(identifier: String): PersonResponse {
        val person = resolvePerson(identifier)
        val pdu = personManagerRepository.findByPersonCrn(person.crn)?.getPDU()
        val setting = if (custodyRepository.isInCustody(personId = person.id)) "Custody" else "Community"
        return person.toPersonResponse(pdu, setting)
    }

    private fun resolvePerson(identifier: String): Person {
        return CRN_REGEX.toRegex().find(identifier)?.let { crn -> personRepository.getPersonByCrn(crn.value) }
            ?: NOMS_REGEX.toRegex().find(identifier)
                ?.let { nomsNumber -> personRepository.getPersonByNoms(nomsNumber.value) }
            ?: throw IllegalArgumentException("${identifier} is not a valid crn or prisoner number")
    }
}

fun Person.toPersonResponse(pdu: ProbationDeliveryUnit?, setting: String) = PersonResponse(
    crn = crn,
    name = Name(forename, listOfNotNull(secondName, thirdName).joinToString(" "), surname),
    nomsNumber = nomsNumber,
    dateOfBirth = dateOfBirth,
    ethnicity = ethnicity?.description,
    gender = gender.description,
    probationDeliveryUnit = pdu,
    setting = setting
)

fun PersonManager.getPDU() = ProbationDeliveryUnit(
    this.team.district.borough.code,
    this.team.district.borough.description
)
