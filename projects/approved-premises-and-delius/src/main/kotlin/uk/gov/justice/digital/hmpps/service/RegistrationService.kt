package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.model.CodeDescription
import uk.gov.justice.digital.hmpps.model.SexualOffenceRegistration
import uk.gov.justice.digital.hmpps.model.SexualOffenceRegistrations

@Service
class RegistrationService(
    val personRepository: PersonRepository,
    val registrationRepository: RegistrationRepository
) {
    fun getSexualOffenceRegistrations(crn: String): SexualOffenceRegistrations {
        personRepository.findByCrnAndSoftDeletedIsFalse(crn).orNotFoundBy( "Person", crn)

        val sexualOffenceCodes = listOf("RSC", "ANSO", "ARSO", "RCHD", "SHPO", "CSEP", "ALT13", "ALT3", "SOPS")
        val registrations = registrationRepository.findByRegistrationCodes(crn, sexualOffenceCodes).map {
            SexualOffenceRegistration(
                type = CodeDescription(
                    code = it.type.code,
                    description = it.type.description
                ),
                category = it.category?.let { category ->
                    CodeDescription(
                        code = category.code,
                        description = category.description
                    )
                } ?: CodeDescription(code = "", description = ""),
                date = it.date,
                nextReviewDate = it.nextReviewDate,
                endDate = it.deregistrations.firstOrNull()?.endDate
            )
        }
        return SexualOffenceRegistrations(
            crn = crn,
            sexualOffenceRegistrations = registrations
        )
    }
}