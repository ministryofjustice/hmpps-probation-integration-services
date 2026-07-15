package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.web.PagedModel.PageMetadata
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.integrations.delius.KeyDateRepository
import uk.gov.justice.digital.hmpps.integrations.delius.Person
import uk.gov.justice.digital.hmpps.integrations.delius.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.StaffRepository
import uk.gov.justice.digital.hmpps.model.*
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class CaseListService(
    private val staffRepository: StaffRepository,
    private val personRepository: PersonRepository,
    private val keyDateRepository: KeyDateRepository,
    private val userAccessService: UserAccessService,
) {
    fun getCaseList(username: String, teamCode: String?, pageable: PageRequest): CaseListResponse {
        val staff = staffRepository.findByUserUsernameIgnoreCase(username).orNotFoundBy("username", username)

        val teamId = teamCode?.let { code ->
            staff.teams
                .filter { it.code.equals(code, ignoreCase = true) }
                .map { it.id }
        }

        val casesPageable = when {
            teamCode == null -> personRepository.findByManagerStaff(staff, pageable)
            !teamId.isNullOrEmpty() -> personRepository.findByManagerTeamIdIn(teamId, pageable)
            else -> Page.empty(pageable)
        }

        val cases = casesPageable.content
        val personIds = cases.map { it.id }
        val crns = cases.map { it.crn }

        val userLimitedAccess =
            userAccessService.userAccessFor(username, crns).access.associateBy { it.crn }
        val caseLimitedAccess =
            userAccessService.checkLimitedAccessFor(crns).access.associate { it.crn to it.isLimitedAccess() }

        val expectedReleaseDates = keyDateRepository.findExpectedReleaseDatesByPersonIdIn(personIds)
            .associate { it.personId to it.releaseDate }

        val responsibleCases = cases.map { person ->
            val access = checkNotNull(userLimitedAccess[person.crn]) { "Access not found for CRN ${person.crn}" }
            val isLimitedAccessCase = caseLimitedAccess[person.crn] ?: false
            person.toCase(access, expectedReleaseDates[person.id], isLimitedAccessCase)
        }

        return CaseListResponse(
            cases = responsibleCases,
            page = PageMetadata(
                pageable.pageSize.toLong(),
                pageable.pageNumber.toLong(),
                casesPageable.totalElements,
                casesPageable.totalPages.toLong()
            )
        )
    }

    fun getCase(username: String, crn: String): Case {
        val person = personRepository.findByCrn(crn).orNotFoundBy("CRN", crn)
        val access = userAccessService.caseAccessFor(username, crn)
        val caseLimitedAccess =
            userAccessService.checkLimitedAccessFor(listOf(crn)).access.single { it.crn == crn }.isLimitedAccess()
        return person.toCase(
            access,
            keyDateRepository.findExpectedReleaseDates(person.id),
            caseLimitedAccess
        )
    }

    private fun CaseAccess.isLimitedAccess() = this.userExcluded || this.userRestricted

    private fun Person.toCase(
        access: CaseAccess,
        expectedReleaseDate: LocalDate?,
        limitedAccess: Boolean,
    ) = Case(
        crn = crn,
        name = Name(
            forename = firstName,
            middleName = listOfNotNull(secondName, thirdName).joinToString(" ").ifEmpty { null },
            surname = surname
        ),
        dateOfBirth = dateOfBirth,
        nomsNumber = noms,
        pncNumber = pnc,
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
        gender = gender.description,
        roshLevel = roshRegistrations
            .firstOrNull { it.type.code in uk.gov.justice.digital.hmpps.integrations.delius.RegisterType.ROSH_CODES }
            ?.type?.let { CodeDescription(it.code, it.description) },
        expectedReleaseDate = expectedReleaseDate,
        userExcluded = access.userExcluded,
        userRestricted = access.userRestricted,
        exclusionMessage = access.exclusionMessage,
        restrictionMessage = access.restrictionMessage,
        limitedAccess = limitedAccess,
    )
}
