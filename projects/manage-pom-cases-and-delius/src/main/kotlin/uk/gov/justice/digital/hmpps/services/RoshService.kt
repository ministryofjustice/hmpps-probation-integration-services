package uk.gov.justice.digital.hmpps.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.RoshCode
import uk.gov.justice.digital.hmpps.api.model.RoshResponse
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.registration.entity.RegistrationRepository

@Service
class RoshService(
    private val registrationRepository: RegistrationRepository
) {

    fun findByIdentifier(crn: String) = registrationRepository.findRoshByPersonCrn(crn)
        ?.let { registration ->
            RoshResponse(
                startDate = registration.date,
                level = RoshCode.fromCode(registration.type.code).name
            )
        }
}