package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.*
import uk.gov.justice.digital.hmpps.model.*

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
        val personManagers = personManagerRepository.findByStaffIdAndActiveTrue(staff.id)
        val personIds = personManagers.map { it.personId }
        val casesById = personRepository.findByIdIn(personIds).associateBy { it.id }
        val roshLevels = registrationRepository.findByPersonIdInAndTypeCodeIn(personIds, RegisterType.ROSH_CODES)
            .groupBy { it.personId }
            .mapValues { (_, reg) -> CodeDescription(reg.first().type.code, reg.first().type.description) }

        val crns = casesById.values.map { it.crn }
        val limitedAccess = userAccessService.userAccessFor(username, crns).access.associateBy { it.crn }

        val responsibleCases = personManagers.mapNotNull {
            val person = casesById[it.personId] ?: return@mapNotNull null
            val access = limitedAccess[person.crn]
            Case(
                crn = person.crn,
                name = Name(
                    person.firstName,
                    listOfNotNull(person.secondName, person.thirdName).joinToString(" "),
                    person.surname
                ),
                dateOfBirth = person.dateOfBirth,
                nomsNumber = person.noms,
                pncNumber = person.pnc,
                staff = Officer(
                    name = Name(
                        forename = it.staff.forename,
                        middleName = it.staff.middleName,
                        surname = it.staff.surname
                    ),
                    username = username,
                    code = it.staff.code
                ),
                team = CodeDescription(
                    code = it.team.code,
                    description = it.team.description
                ),
                gender = person.gender.description,
                roshLevel = roshLevels[person.id],
                expectedReleaseDate = keyDateRepository.findExpectedReleaseDates(person.id),
                userExcluded = access?.userExcluded ?: false,
                userRestricted = access?.userRestricted ?: false,
                exclusionMessage = access?.exclusionMessage,
                restrictionMessage = access?.restrictionMessage
            )
        }

        return CaseListResponse(responsibleCases)
    }
}