package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.integrations.delius.*
import uk.gov.justice.digital.hmpps.model.*
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class CaseListService(
    private val staffRepository: StaffRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val personRepository: PersonRepository,
    private val keyDateRepository: KeyDateRepository,
    private val registrationRepository: RegistrationRepository,
    private val userAccessService: UserAccessService
) {
    fun getCaseList(username: String): CaseListResponse {
        val staff = staffRepository.findByUserUsernameIgnoreCase(username) ?: throw NotFoundException(
            "Staff",
            "username",
            username
        )
        val teamIds = staff.teams.map { it.id }
        val personManagers = if (teamIds.isNotEmpty()) personManagerRepository.findByTeamIdIn(teamIds) else emptyList()
        val personIds = personManagers.map { it.personId }
        val casesById = personRepository.findByIdIn(personIds).associateBy { it.id }
        val roshLevels = registrationRepository.findByPersonIdInAndTypeCodeIn(personIds, RegisterType.ROSH_CODES)
            .groupBy { it.personId }
            .mapValues { (_, reg) -> CodeDescription(reg.first().type.code, reg.first().type.description) }

        val crns = casesById.values.map { it.crn }
        val limitedAccess = userAccessService.userAccessFor(username, crns).access.associateBy { it.crn }
        val expectedReleaseDates = keyDateRepository.findExpectedReleaseDatesByPersonIdIn(personIds)
            .associate { it.personId to it.releaseDate }

        val responsibleCases = personManagers.mapNotNull {
            val person = casesById[it.personId] ?: return@mapNotNull null
            val access = checkNotNull(limitedAccess[person.crn]) { "Access not found for CRN ${person.crn}" }
            val roshLevel = roshLevels[person.id]
            toCase(person, it, access, roshLevel, expectedReleaseDates[person.id])
        }

        return CaseListResponse(responsibleCases)
    }

    fun getCase(username: String, crn: String): Case {
        val person = personRepository.findByCrn(crn).orNotFoundBy("CRN", crn)

        val manager = personManagerRepository.findFirstByPersonId(person.id)
            .orNotFoundBy("personId", person.id)

        val roshLevel = registrationRepository
            .findByPersonIdInAndTypeCodeIn(listOf(person.id), RegisterType.ROSH_CODES)
            .firstOrNull()
            ?.let { CodeDescription(it.type.code, it.type.description) }

        val access = userAccessService.caseAccessFor(username, crn)

        return toCase(person, manager, access, roshLevel, keyDateRepository.findExpectedReleaseDates(person.id))
    }

    private fun toCase(
        person: Person,
        manager: PersonManager,
        access: CaseAccess,
        roshLevel: CodeDescription?,
        expectedReleaseDate: LocalDate?
    ) = Case(
        crn = person.crn,
        name = Name(
            forename = person.firstName,
            middleName = listOfNotNull(person.secondName, person.thirdName).joinToString(" ").ifEmpty { null },
            surname = person.surname
        ),
        dateOfBirth = person.dateOfBirth,
        nomsNumber = person.noms,
        pncNumber = person.pnc,
        staff = Officer(
            name = Name(
                forename = manager.staff.forename,
                middleName = manager.staff.middleName,
                surname = manager.staff.surname
            ),
            username = manager.staff.user?.username ?: "",
            code = manager.staff.code
        ),
        team = CodeDescription(
            code = manager.team.code,
            description = manager.team.description
        ),
        gender = person.gender.description,
        roshLevel = roshLevel,
        expectedReleaseDate = expectedReleaseDate,
        userExcluded = access.userExcluded,
        userRestricted = access.userRestricted,
        exclusionMessage = access.exclusionMessage,
        restrictionMessage = access.restrictionMessage
    )
}