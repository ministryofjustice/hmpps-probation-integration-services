package uk.gov.justice.digital.hmpps.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.digital.hmpps.entity.PersonRepository
import uk.gov.justice.digital.hmpps.model.PersonalDetails
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Service
class PersonalDetailsValidationService(
    val personRepository: PersonRepository,
    val telemetryService: TelemetryService
) {
    fun validatePersonalDetails(personalDetails: PersonalDetails) {
        val person = personRepository.findByCrn(personalDetails.crn)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "CRN not found")
        val firstNameValid = person.firstName matches personalDetails.name.forename
        val lastNameValid = person.lastName matches personalDetails.name.surname
        val dateOfBirthValid = person.dateOfBirth.isEqual(personalDetails.dateOfBirth)
        if (!firstNameValid || !lastNameValid || !dateOfBirthValid) {
            telemetryService.trackEvent(
                "PersonalDetailsValidationFailed", mapOf(
                    "crn" to personalDetails.crn,
                    "firstNameValid" to firstNameValid.toString(),
                    "lastNameValid" to lastNameValid.toString(),
                    "dobValid" to dateOfBirthValid.toString()
                )
            )
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Validation failed")
        }
    }

    fun String.normalise() = replace(Regex("[^A-Za-z0-9]"), "")
    infix fun String.matches(other: String) = normalise().equals(other.normalise(), ignoreCase = true)
}