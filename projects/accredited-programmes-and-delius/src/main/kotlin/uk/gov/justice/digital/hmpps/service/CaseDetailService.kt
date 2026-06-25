package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.staff.Team
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.ldap.findEmailByUsername
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.repository.*
import uk.gov.justice.digital.hmpps.entity.sentence.component.LicenceCondition as LicenceConditionEntity
import uk.gov.justice.digital.hmpps.entity.sentence.component.Requirement as RequirementEntity

@Service
class CaseDetailService(
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
    private val ldapTemplate: LdapTemplate,
    private val offenceRepository: OffenceRepository,
    private val registrationRepository: RegistrationRepository,
    private val officeLocationRepository: OfficeLocationRepository,
    private val licenceConditionRepository: LicenceConditionRepository,
    private val requirementRepository: RequirementRepository,
) {
    fun getPersonalDetails(crn: String) = personRepository.getByCrn(crn).let { person ->
        checkNotNull(person.manager) { "Person does not have an active manager" }
        PersonalDetails(
            crn = person.crn,
            name = Name(
                forename = person.forename,
                surname = person.surname,
                middleNames = listOfNotNull(person.secondName, person.thirdName).joinToString(" ")
            ),
            dateOfBirth = person.dateOfBirth,
            sex = person.gender.toCodedValue(),
            ethnicity = person.ethnicity?.toCodedValue(),
            probationPractitioner = person.manager.staff.toProbationPractitioner { ldapTemplate.findEmailByUsername(it.username) },
            team = person.manager.team.toCodedValue(),
            probationDeliveryUnit = person.manager.team.localAdminUnit.probationDeliveryUnit.toCodedValue(),
            region = with(person.manager.team.provider) { CodedValue(code, description) }
        )
    }

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

    fun getRequirement(crn: String, id: Long): Requirement {
        if (!personRepository.existsByCrn(crn)) throw NotFoundException("Person", "crn", crn)
        val requirement = requirementRepository.findByIdOrNotFound(id)
        require(requirement.disposal.event.person.crn == crn) { "CRN does not match requirement" }
        return requirement.toModel()
    }

    fun getRequirements(crn: String): Requirements {
        if (!personRepository.existsByCrn(crn)) throw NotFoundException("Person", "crn", crn)
        val requirements = requirementRepository.findAllByDisposalEventPersonCrnAndMainCategoryCodeIn(crn)
        return Requirements(requirements.map { it.toModel() })
    }

    fun getLicenceCondition(crn: String, id: Long): LicenceCondition {
        if (!personRepository.existsByCrn(crn)) throw NotFoundException("Person", "crn", crn)
        val licenceCondition = licenceConditionRepository.findByIdOrNotFound(id)
        require(licenceCondition.disposal.event.person.crn == crn) { "CRN does not match licence condition" }
        return licenceCondition.toModel()
    }

    fun getLicenceConditions(crn: String): LicenceConditions {
        if (!personRepository.existsByCrn(crn)) throw NotFoundException("Person", "crn", crn)
        val licenceConditions = licenceConditionRepository.findAllByDisposalEventPersonCrnAndMainCategoryCodeIn(crn)
        return LicenceConditions(content = licenceConditions.map { it.toModel() })
    }

    private fun Team.pduOfficeLocations() =
        officeLocationRepository.findByRegionId(localAdminUnit.probationDeliveryUnit.regionId)
            .groupBy { it.localAdminUnit.probationDeliveryUnit.toCodedValue() }
            .map { (key, value) -> PduOfficeLocations(key.code, key.description, value.map { it.toCodedValue() }) }

    private fun LicenceConditionEntity.toModel(): LicenceCondition {
        val manager = checkNotNull(manager) { "Licence condition does not have an active manager" }
        return LicenceCondition(
            id = id,
            mainCategory = mainCategory.toCodedValue(),
            subCategory = subCategory?.toCodedValue(),
            manager = Manager(
                staff = manager.staff.toProbationPractitioner { ldapTemplate.findEmailByUsername(it.username) },
                team = manager.team.toCodedValue(),
                probationDeliveryUnit = manager.team.localAdminUnit.probationDeliveryUnit.toCodedValue(),
                officeLocations = manager.team.officeLocations.map { it.toCodedValue() }
            ),
            probationDeliveryUnits = manager.team.pduOfficeLocations(),
            eventNumber = manager.licenceCondition.disposal.event.number,
            createdAt = createdDatetime
        )
    }

    private fun RequirementEntity.toModel(): Requirement {
        val manager = checkNotNull(manager) { "Requirement does not have an active manager" }
        return Requirement(
            id = id,
            mainCategory = mainCategory?.toCodedValue(),
            subCategory = subCategory?.toCodedValue(),
            manager = Manager(
                staff = manager.staff.toProbationPractitioner { ldapTemplate.findEmailByUsername(it.username) },
                team = manager.team.toCodedValue(),
                probationDeliveryUnit = manager.team.localAdminUnit.probationDeliveryUnit.toCodedValue(),
                officeLocations = manager.team.officeLocations.map { it.toCodedValue() }
            ),
            probationDeliveryUnits = manager.team.pduOfficeLocations(),
            eventNumber = manager.requirement.disposal.event.number,
            createdAt = createdDatetime
        )
    }
}