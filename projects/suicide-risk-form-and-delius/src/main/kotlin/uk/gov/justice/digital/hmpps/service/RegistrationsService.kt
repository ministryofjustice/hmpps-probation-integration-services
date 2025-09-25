package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.codedDescription
import uk.gov.justice.digital.hmpps.integrations.delius.findLatestRegistration
import uk.gov.justice.digital.hmpps.model.InformationPageResponse
import uk.gov.justice.digital.hmpps.model.MostRecentRegistration

@Service
class RegistrationsService(
    private val registrationRepository: RegistrationRepository,
    private val personRepository: PersonRepository
) {
    fun informationPage(crn: String): InformationPageResponse {
        if (!personRepository.existsByCrn(crn))
            throw NotFoundException("Person with CRN $crn not found")

        val latestRegistration = registrationRepository.findLatestRegistration(crn)
            ?: return InformationPageResponse(registration = null)

        return InformationPageResponse(
            registration = MostRecentRegistration(
                id = latestRegistration.id,
                type = latestRegistration.type.codedDescription(),
                startDate = latestRegistration.date,
                endDate = latestRegistration.deregistrations.minOfOrNull { it.deRegistrationDate },
                notes = latestRegistration.notes,
                documentsLinked = latestRegistration.documentLinked ?: false,
                deregistered = latestRegistration.deregistered
            )
        )
    }
}