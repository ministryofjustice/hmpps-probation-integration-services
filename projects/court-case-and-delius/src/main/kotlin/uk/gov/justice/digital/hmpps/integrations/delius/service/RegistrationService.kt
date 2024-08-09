package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.KeyValue
import uk.gov.justice.digital.hmpps.api.model.Registrations
import uk.gov.justice.digital.hmpps.integration.delius.registration.entity.Registration
import uk.gov.justice.digital.hmpps.integration.delius.registration.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getPerson

@Service
class RegistrationService(
    private val personRepository: PersonRepository,
    private val registrationRepository: RegistrationRepository,
) {

    fun getRegistrationsFor(crn: String, activeOnly: Boolean): Registrations {
        val person = personRepository.getPerson(crn)
        val registrations =
            if (activeOnly) registrationRepository.findActiveByPersonId(person.id)
            else registrationRepository.findByPersonId(person.id)

        return Registrations(registrations.map(Registration::toRegistration))
    }
}

fun Registration.toRegistration() = uk.gov.justice.digital.hmpps.api.model.Registration(
    offenderId = person.id,
    register = type.flag.let { KeyValue(it.code, it.description) },
    type = KeyValue(type.code, type.description),
    riskColour = type.colour,
    startDate = date,
    nextReviewDate = reviewDate,
    reviewPeriodMonths = type.reviewPeriod,
    notes = notes,
    registeringTeam = KeyValue(team.code, team.description),
    registeringOfficer = staff.toStaffHuman(),
    registeringProbationArea = KeyValue(team.probationArea.code, team.probationArea.description),
    registerLevel = level?.let { KeyValue(it.code, it.description) },
    registerCategory = category?.let { KeyValue(it.code, it.description) },
    warnUser = type.alertMessage,
    active = !deRegistered,
    endDate = latestDeregistration()?.deRegistrationDate,
    deregisteringTeam = latestDeregistration()?.team?.let { KeyValue(it.code, it.description) }.takeIf { deRegistered },
    deregisteringOfficer = latestDeregistration()?.staff?.toStaffHuman().takeIf { deRegistered },
    deregisteringProbationArea = latestDeregistration()?.team?.probationArea?.let { KeyValue(it.code, it.description) }
        .takeIf { deRegistered },
    deregisteringNotes = latestDeregistration()?.notes,
    numberOfPreviousDeregistrations = deRegistrations.size

)