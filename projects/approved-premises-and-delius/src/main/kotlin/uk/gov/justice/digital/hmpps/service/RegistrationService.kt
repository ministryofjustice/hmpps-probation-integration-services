package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException
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
        if (!personRepository.existsByCrnAndSoftDeletedIsFalse(crn)) {
            throw NotFoundException("Person", "crn", crn)
        }

        val registrations = registrationRepository.findByRegistrationCodes(
            crn,
            SexualOffenceRegistrationCode.entries.map { it.code }
        ).map {
            SexualOffenceRegistration(
                type = CodeDescription(
                    code = it.type.code,
                    description = it.type.description
                ),
                category = category?.run { CodeDescription(code, description) },
                startDate = it.date,
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

enum class SexualOffenceRegistrationCode(val code: String, val description: String) {
    RSC("RSC", "(SC1 & SC2)"),
    ANSO("ANSO", "Non Registered Sex Offender"),
    ARSO("ARSO", "Registered Sex Offender"),
    RCHD("RCHD", "Risk to Children"),
    SHPO("SHPO", "Sexual Harm Prevention Order / Sexual Risk Order"),
    CSEP("CSEP", "Child Sexual Exploitation – Perpetrator"),
    ALT13("ALT13", "ALT Child Sexual Exploitation history"),
    ALT3("ALT3", "ALT Registered Sex Offender"),
    SOPS("SOPS", "Sex Offences Prevention Order")
}

