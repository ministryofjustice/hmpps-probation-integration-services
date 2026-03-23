package uk.gov.justice.digital.hmpps.services

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.RoshCode
import uk.gov.justice.digital.hmpps.api.model.RoshResponse
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.registration.entity.RegistrationRepository

@Service
class RoshService(
    private val registrationRepository: RegistrationRepository
) {

    fun findByIdentifier(crn: String): RoshResponse? {

        return registrationRepository.findRoshByPersonCrn(crn, PageRequest.of(0, 1)).get(0)
            .let { registration ->
                RoshResponse(
                    startDate = registration.date,
                    level = RoshCode.fromCode(registration.type.code).name
                )
            }
    }
}