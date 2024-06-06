package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.user.*
import uk.gov.justice.digital.hmpps.api.model.user.Staff
import uk.gov.justice.digital.hmpps.api.model.user.Team
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val caseloadRepository: CaseloadRepository,
    private val staffRepository: StaffRepository,
    private val teamRepository: TeamRepository
) {

    @Transactional
    fun getUserCaseload(username: String, pageable: Pageable): StaffCaseload {
        val user = userRepository.getUser(username)
        val caseload = caseloadRepository.findByStaffCode(user.staff!!.code, pageable)
        return StaffCaseload(
            totalElements = caseload.totalElements.toInt(),
            totalPages = caseload.totalPages,
            provider = user.staff.provider.description,
            caseload = caseload.content.map { it.toStaffCase() },
            staff = Name(forename = user.staff.forename, surname = user.staff.surname)
        )
    }

    @Transactional
    fun getTeamCaseload(teamCode: String, pageable: Pageable): TeamCaseload {
        val team = teamRepository.getTeam(teamCode)
        val caseload = caseloadRepository.findByTeamCode(team.code, pageable)
        caseload.content
        return TeamCaseload(
            totalElements = caseload.totalElements.toInt(),
            totalPages = caseload.totalPages,
            provider = team.provider.description,
            caseload = caseload.content.map { it.toTeamCase() },
            team = Team(description = team.description, team.code)
        )
    }

    @Transactional
    fun getUserTeams(username: String): UserTeam {
        val user = userRepository.getUser(username)
        val teams = staffRepository.findTeamsByStaffCode(user.staff!!.code)
            .map { Team(description = it.description, code = it.code) }
        return UserTeam(provider = user.staff.provider.description, teams = teams)
    }

    @Transactional
    fun getTeamStaff(teamCode: String): TeamStaff {
        val provider = teamRepository.getProvider(teamCode)
        val staff = teamRepository.findStaffByTeamCode(teamCode)
            .map { Staff(name = Name(forename = it.forename, surname = it.surname), code = it.code) }
        return TeamStaff(provider = provider, staff = staff)
    }
}

fun Caseload.toStaffCase() = StaffCase(
    caseName = Name(
        forename = person.forename,
        middleName = listOfNotNull(person.secondName, person.thirdName).joinToString(" "),
        surname = person.surname
    ),
    crn = person.crn,
)

fun Caseload.toTeamCase() = TeamCase(
    caseName = Name(
        forename = person.forename,
        middleName = listOfNotNull(person.secondName, person.thirdName).joinToString(" "),
        surname = person.surname
    ),
    crn = person.crn,
    staff = Staff(name = Name(forename = staff.forename, surname = staff.surname), code = staff.code)
)
