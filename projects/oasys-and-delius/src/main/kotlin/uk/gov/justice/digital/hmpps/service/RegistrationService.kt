package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.integration.delius.provider.entity.Provider
import uk.gov.justice.digital.hmpps.integration.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integration.delius.provider.entity.Team
import uk.gov.justice.digital.hmpps.integration.delius.reference.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integration.delius.registration.entity.RegisterType
import uk.gov.justice.digital.hmpps.integration.delius.registration.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.integration.delius.registration.entity.RegistrationReview

@Service
class RegistrationService(private val registrationRepository: RegistrationRepository) {
    fun findActiveRegistrations(crn: String) = Registrations(
        registrationRepository.findAllByPersonCrnOrderByDateDescCreatedDateTimeDesc(crn).map { it.toModel() }
    )
}

fun uk.gov.justice.digital.hmpps.integration.delius.registration.entity.Registration.toModel(): Registration =
    Registration(
        id,
        person.id,
        type.flag?.codeDescription(),
        type.codeDescription(),
        type.colour,
        date,
        reviewDate,
        type.reviewPeriod,
        notes,
        team.codeDescription(),
        staff.asOfficer(),
        team.provider.codeDescription(),
        level?.codeDescription(),
        category?.codeDescription(),
        reviews.map { it.toModel() }
    )

fun ReferenceData.codeDescription() = CodeDescription(code, description)
fun RegisterType.codeDescription() = CodeDescription(code, description)
fun Provider.codeDescription() = CodeDescription(code, description)
fun Team.codeDescription() = CodeDescription(code, description)
fun Staff.asOfficer() = Officer(code, forename, surname)
fun RegistrationReview.toModel() = Review(
    date, reviewDue, notes, team.codeDescription(), staff.asOfficer(), completed
)