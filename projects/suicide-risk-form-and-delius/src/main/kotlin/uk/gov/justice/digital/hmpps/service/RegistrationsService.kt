package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.codedDescription
import uk.gov.justice.digital.hmpps.model.InformationPageResponse
import uk.gov.justice.digital.hmpps.model.MostRecentRegistration

@Service
class RegistrationsService(
    private val registrationRepository: RegistrationRepository
) {
    fun informationPage(crn: String): InformationPageResponse {
        val latestRegistration = registrationRepository.findLatestRelevantRegistrationForCrn(crn)
            ?: throw NotFoundException("No registration found for CRN: $crn")

        return InformationPageResponse(
            registration = MostRecentRegistration(
                id = latestRegistration.id,
                type = latestRegistration.type.codedDescription(),
                startDate = latestRegistration.date,
                endDate = latestRegistration.deregistrations.minByOrNull { it.deRegistrationDate }?.deRegistrationDate,
                notes = latestRegistration.notes,
                documentsLinked = latestRegistration.documentLinked,
                deregistered = latestRegistration.deregistered
            )
        )
    }
}