package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.person.PersonRepository
import uk.gov.justice.digital.hmpps.entity.person.RegistrationRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.CodeDescription
import uk.gov.justice.digital.hmpps.model.RegistrationResponse

@Service
class RegistrationService(
    private val personRepository: PersonRepository,
    private val registrationRepository: RegistrationRepository,
) {
    fun getRegistrations(crn: String): RegistrationResponse {
        val registrations = registrationRepository.findByPerson_CrnOrderByType_Code(crn)
            .map {
                CodeDescription(
                    code = it.type.code,
                    description = it.type.description
                )
            }
        if (registrations.isEmpty() && !personRepository.existsByCrn(crn)) {
            throw NotFoundException("Person", "CRN", crn)
        }
        return RegistrationResponse(registrations)
    }
}