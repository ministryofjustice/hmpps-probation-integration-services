package uk.gov.justice.digital.hmpps.service

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
    fun getUserCaseload(username: String): StaffCaseload {
        val user = userRepository.getUser(username)
        val caseload = caseloadRepository.findByStaffCode(user.staff!!.code)
        return StaffCaseload(
            provider = user.staff.provider.description,
            caseload = caseload.map { it.toStaffCase() },
            staff = Name(forename = user.staff.forename, surname = user.staff.surname)
        )
    }

    @Transactional
    fun getStaffCaseload(staffCode: String): StaffCaseload {
        val staff = staffRepository.getStaff(staffCode)
        val caseload = caseloadRepository.findByStaffCode(staff.code)
        return StaffCaseload(
            provider = staff.provider.description,
            caseload = caseload.map { it.toStaffCase() },
            staff = Name(forename = staff.forename, surname = staff.surname)
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

