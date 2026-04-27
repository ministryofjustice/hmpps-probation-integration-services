package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.integrations.delius.KeyDateRepository
import uk.gov.justice.digital.hmpps.integrations.delius.Person
import uk.gov.justice.digital.hmpps.integrations.delius.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.StaffRepository
import uk.gov.justice.digital.hmpps.model.*

@Service
@Transactional(readOnly = true)
class CaseListService(
    private val staffRepository: StaffRepository,
    private val personRepository: PersonRepository,
    private val keyDateRepository: KeyDateRepository,
    private val userAccessService: UserAccessService,
) {
    fun getCaseList(username: String): CaseListResponse {
        val staff = staffRepository.findByUserUsernameIgnoreCase(username).orNotFoundBy("username", username)
        val cases = personRepository.findByManagerTeamIdIn(staff.teams.map { it.id })
        val limitedAccess =
            userAccessService.userAccessFor(username, cases.map { it.crn }).access.associateBy { it.crn }

        val responsibleCases = cases.map { person ->
            val access = checkNotNull(limitedAccess[person.crn]) { "Access not found for CRN ${person.crn}" }
            person.toCase(access)
        }

        return CaseListResponse(responsibleCases)
    }

    fun getCase(username: String, crn: String): Case {
        val person = personRepository.findByCrn(crn).orNotFoundBy("CRN", crn)
        val access = userAccessService.caseAccessFor(username, crn)
        return person.toCase(access)
    }

    private fun Person.toCase(
        access: CaseAccess,
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
        roshLevel = roshRegistrations.firstOrNull()?.let { CodeDescription(it.type.code, it.type.description) },
        expectedReleaseDate = keyDateRepository.findExpectedReleaseDates(id),
        userExcluded = access.userExcluded,
        userRestricted = access.userRestricted,
        exclusionMessage = access.exclusionMessage,
        restrictionMessage = access.restrictionMessage
    )
}