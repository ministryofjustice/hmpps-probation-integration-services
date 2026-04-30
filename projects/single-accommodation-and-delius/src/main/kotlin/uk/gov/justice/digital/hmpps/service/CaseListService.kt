package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.web.PagedModel.PageMetadata
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.integrations.delius.*
import uk.gov.justice.digital.hmpps.model.*
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class CaseListService(
    private val staffRepository: StaffRepository,
    private val personRepository: PersonRepository,
    private val keyDateRepository: KeyDateRepository,
    private val registrationRepository: RegistrationRepository,
    private val userAccessService: UserAccessService,
) {
    fun getCaseList(username: String, pageable: PageRequest): CaseListResponse {
        val staff = staffRepository.findByUserUsernameIgnoreCase(username).orNotFoundBy("username", username)
        val teamIds = staff.teams.map { it.id }
        val casesPageable = if (teamIds.isNotEmpty())
            personRepository.findByManagerTeamIdIn(teamIds, pageable)
        else Page.empty(pageable)

        val cases = casesPageable.content
        val personIds = cases.map { it.id }
        val limitedAccess =
            userAccessService.userAccessFor(username, cases.map { it.crn }).access.associateBy { it.crn }
        val expectedReleaseDates = keyDateRepository.findExpectedReleaseDatesByPersonIdIn(personIds)
            .associate { it.personId to it.releaseDate }

        val responsibleCases = cases.map { person ->
            val access = checkNotNull(limitedAccess[person.crn]) { "Access not found for CRN ${person.crn}" }
            person.toCase(access, expectedReleaseDates[person.id])
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
        val roshLevel = person.roshRegistrations.firstOrNull()
            ?.let { CodeDescription(it.type.code, it.type.description) }
        return person.toCase(access, keyDateRepository.findExpectedReleaseDates(person.id))
    }

    private fun Person.toCase(
        access: CaseAccess,
        expectedReleaseDate: LocalDate?,
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
        roshLevel = roshRegistrations.firstOrNull()?.type?.let { CodeDescription(it.code, it.description) },
        expectedReleaseDate = expectedReleaseDate,
        userExcluded = access.userExcluded,
        userRestricted = access.userRestricted,
        exclusionMessage = access.exclusionMessage,
        restrictionMessage = access.restrictionMessage
    )
}
