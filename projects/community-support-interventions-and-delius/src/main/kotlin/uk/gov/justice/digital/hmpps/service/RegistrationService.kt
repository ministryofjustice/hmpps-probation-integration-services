package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.controller.model.HomeOfficeInterest
import uk.gov.justice.digital.hmpps.entity.PersonRepository
import uk.gov.justice.digital.hmpps.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy

@Service
class RegistrationService(
    val personRepository: PersonRepository,
    val registrationRepository: RegistrationRepository,
) {

    fun getHomeOfficeInterest(crn: String): HomeOfficeInterest {
        val person = personRepository.findByCrn(crn).orNotFoundBy("Person", crn)
        return registrationRepository.findHomeOfficeInterestByPersonId(person.id).let { registration ->
            HomeOfficeInterest(
                exists = registration != null,
                notes = registration?.notes
            )
        }
    }
}