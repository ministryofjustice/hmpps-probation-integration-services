package uk.gov.justice.digital.hmpps.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.digital.hmpps.integrations.delius.PersonRepository
import uk.gov.justice.digital.hmpps.model.PersonalDetails

@Service
class PersonalDetailsValidationService(val personRepository: PersonRepository) {
    fun validatePersonalDetails(personalDetails: PersonalDetails) {
        val person = personRepository.findByCrn(personalDetails.crn)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "CRN not found")
        if (!person.firstName.trim().equals(personalDetails.name.forename.trim(), ignoreCase = true) ||
            !person.lastName.trim().equals(personalDetails.name.surname.trim(), ignoreCase = true) ||
            !person.dateOfBirth.isEqual(personalDetails.dateOfBirth)
        ) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Validation failed")
        }
    }
}