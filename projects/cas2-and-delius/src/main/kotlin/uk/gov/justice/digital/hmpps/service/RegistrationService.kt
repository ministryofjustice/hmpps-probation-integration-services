package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.client.approvedpremises.model.CodeDescription
import uk.gov.justice.digital.hmpps.client.approvedpremises.model.SexualOffenceRegistration
import uk.gov.justice.digital.hmpps.client.approvedpremises.model.SexualOffenceRegistrations
import uk.gov.justice.digital.hmpps.entity.PersonRepository
import uk.gov.justice.digital.hmpps.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.entity.getByCrn

@Service
class RegistrationService(
    val personRepository: PersonRepository,
    val registrationRepository: RegistrationRepository
) {
    fun getSexualOffenceRegistrations(crn: String): SexualOffenceRegistrations {
        personRepository.getByCrn(crn)

        val sexualOffenceCodes = listOf("RSC", "ANSO", "ARSO", "RCHD", "SHPO", "CSEP", "ALT13", "ALT3", "SOPS")
        val registrations = registrationRepository.findByRegistrationCodes(crn, sexualOffenceCodes).map {
            SexualOffenceRegistration(
                type = CodeDescription(code = it.registerType.code, description = it.registerType.description),
                category = CodeDescription(code = it.category.code, description = it.category.description),
                date = it.registrationDate,
                nextReviewDate = it.nextReviewDate,
                endDate = it.deregistration?.endDate,
            )
        }
        return SexualOffenceRegistrations(
            crn = crn,
            sexualOffenceRegistrations = registrations
        )
    }
}