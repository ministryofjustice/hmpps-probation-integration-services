package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.ldap.findEmailByUsername
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.repository.*

@Service
class CaseDetailService(
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
    private val ldapTemplate: LdapTemplate,
    private val offenceRepository: OffenceRepository,
    private val registrationRepository: RegistrationRepository,
    private val requirementManagerRepository: RequirementManagerRepository,
) {
    fun getPersonalDetails(crn: String) = personRepository.findByCrn(crn)?.let { person ->
        checkNotNull(person.manager) { "Person does not have an active manager" }
        PersonalDetails(
            crn = person.crn,
            name = Name(
                forename = person.forename,
                middleNames = listOfNotNull(person.secondName, person.thirdName).joinToString(" "),
                surname = person.surname
            ),
            dateOfBirth = person.dateOfBirth,
            sex = person.gender.toCodedValue(),
            ethnicity = person.ethnicity?.toCodedValue(),
            probationPractitioner = person.manager.staff.toProbationPractitioner { ldapTemplate.findEmailByUsername(it.username) },
            team = person.manager.team.toCodedValue(),
            probationDeliveryUnit = person.manager.team.localAdminUnit.probationDeliveryUnit.toCodedValue()
        )
    } ?: throw NotFoundException("Person", "crn", crn)

    fun getSentence(crn: String, eventNumber: String) =
        eventRepository.findByPersonCrnAndNumber(crn, eventNumber)?.let { event ->
            requireNotNull(event.disposal) { "Event is not sentenced" }
            Sentence(
                description = event.disposal.description(),
                startDate = event.disposal.date,
                expectedEndDate = event.disposal.expectedEndDate(),
                licenceExpiryDate = event.disposal.custody?.licenceEndDate(),
                postSentenceSupervisionEndDate = event.disposal.custody?.postSentenceSupervisionEndDate(),
                twoThirdsSupervisionDate = event.disposal.custody?.probationResetDate() ?: event.twoThirdsDate(),
                custodial = event.disposal.type.isCustodial(),
                releaseType = event.disposal.custody?.mostRecentRelease()?.type?.description,
                licenceConditions = event.disposal.licenceConditions.map {
                    it.subCategory?.toCodedValue() ?: it.mainCategory.toCodedValue()
                },
                requirements = event.disposal.requirements.mapNotNull {
                    it.subCategory?.toCodedValue() ?: it.mainCategory?.toCodedValue()
                },
                postSentenceSupervisionRequirements = event.disposal.custody?.postSentenceSupervisionRequirements?.map {
                    it.subCategory?.toCodedValue() ?: it.mainCategory.toCodedValue()
                } ?: emptyList(),
            )
        } ?: throw NotFoundException("Event for $crn", "number", eventNumber)

    fun getOffences(crn: String, eventNumber: String) =
        offenceRepository.findByPersonCrnAndNumber(crn, eventNumber)?.let { event ->
            Offences(
                event.mainOffence.map { it.offence.toOffence(it.date) }.single(),
                event.additionalOffences.map { it.offence.toOffence(it.date) },
            )
        } ?: throw NotFoundException("Event for $crn", "number", eventNumber)

    fun getRegistrations(crn: String) = Registrations(
        registrationRepository.findByPersonCrn(crn).map { registration ->
            Registration(
                type = CodedValue(registration.type.code, registration.type.description),
                category = registration.category?.toCodedValue(),
                date = registration.date,
                nextReviewDate = registration.nextReviewDate,
            )
        }
    )

    fun getRequirement(id: Long) = requirementManagerRepository.findByRequirementId(id)?.let { manager ->
        Requirement(
            manager = Manager(
                staff = manager.staff.toProbationPractitioner { ldapTemplate.findEmailByUsername(it.username) },
                team = manager.team.toCodedValue(),
                probationDeliveryUnit = manager.team.localAdminUnit.probationDeliveryUnit.toCodedValue(),
                officeLocations = manager.team.officeLocations.map { it.toCodedValue() }
            )
        )
    } ?: throw NotFoundException("Requirement", "id", id)
}
