package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.web.PagedModel
import org.springframework.data.web.PagedModel.PageMetadata
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.entity.person.Person
import uk.gov.justice.digital.hmpps.entity.person.RegisterType
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.repository.KeyDateRepository
import uk.gov.justice.digital.hmpps.repository.PersonRepository
import uk.gov.justice.digital.hmpps.repository.StaffRepository
import uk.gov.justice.digital.hmpps.repository.TeamRepository
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class CaseListService(
    private val staffRepository: StaffRepository,
    private val personRepository: PersonRepository,
    private val keyDateRepository: KeyDateRepository,
    private val userAccessService: UserAccessService,
    private val teamRepository: TeamRepository,
) {
    fun getCaseList(username: String, teamCode: String?, pageable: PageRequest): CaseListResponse {
        val staff = staffRepository.findByUserUsernameIgnoreCase(username).orNotFoundBy("username", username)

        val caseloadPage = when (teamCode) {
            null -> personRepository.findByCaseloadStaffId(staff.id, pageable)
            in staff.teams.map { it.code } -> personRepository.findByCaseloadTeamCode(teamCode, pageable)
            else -> Page.empty(pageable)
        }

        val cases = caseloadPage.content
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
            val isLimitedAccessCase = caseLimitedAccess[person.crn]
            person.toCase(access, expectedReleaseDates[person.id], isLimitedAccessCase)
        }

        return CaseListResponse(
            cases = responsibleCases,
            page = PageMetadata(
                pageable.pageSize.toLong(),
                pageable.pageNumber.toLong(),
                caseloadPage.totalElements,
                caseloadPage.totalPages.toLong()
            )
        )
    }

    fun getTeamCaseIds(teamCode: String, pageable: PageRequest) = if (teamRepository.existsByCode(teamCode)) {
        PagedModel(personRepository.findCaseIdentifiersByTeamCode(teamCode, pageable))
    } else throw NotFoundException("Team", "code", teamCode)

    fun getCase(username: String, crn: String): Case {
        val access = userAccessService.caseAccessFor(username, crn)
        return getCaseByAccess(access)
    }

    fun getCase(crn: String): Case {
        val access = CaseAccess(crn = crn, userExcluded = null, userRestricted = null)
        return getCaseByAccess(access)
    }

    private fun getCaseByAccess(access: CaseAccess): Case {
        val person = personRepository.findByCrn(access.crn).orNotFoundBy("CRN", access.crn)
        val caseLimitedAccess =
            userAccessService.checkLimitedAccessFor(listOf(access.crn)).access.single { it.crn == access.crn }
                .isLimitedAccess()
        return person.toCase(
            access,
            keyDateRepository.findExpectedReleaseDates(person.id),
            caseLimitedAccess
        )
    }

    private fun CaseAccess.isLimitedAccess(): Boolean? = when {
        userExcluded == true || userRestricted == true -> true
        userExcluded == null || userRestricted == null -> null
        else -> false
    }

    private fun Person.toCase(
        access: CaseAccess?,
        expectedReleaseDate: LocalDate?,
        limitedAccess: Boolean?,
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
        roshLevel = roshRegistrations.firstOrNull { it.type.code in RegisterType.ROSH_CODES }?.type
            ?.let { CodeDescription(it.code, it.description) },
        expectedReleaseDate = expectedReleaseDate,
        userExcluded = access?.userExcluded,
        userRestricted = access?.userRestricted,
        exclusionMessage = access?.exclusionMessage,
        restrictionMessage = access?.restrictionMessage,
        limitedAccess = if (limitedAccess == true) true else null,
    )
}