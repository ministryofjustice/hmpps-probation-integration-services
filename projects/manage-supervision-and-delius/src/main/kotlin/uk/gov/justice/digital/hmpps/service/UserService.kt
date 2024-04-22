package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.user.StaffCase
import uk.gov.justice.digital.hmpps.api.model.user.Team
import uk.gov.justice.digital.hmpps.api.model.user.User
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.Caseload
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.UserRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.getUser

@Service
class UserService(
    private val userRepository: UserRepository
) {

    @Transactional
    fun getUserDetails(username: String): User {
        return userRepository.getUser(username).toUser()
    }
}

fun Caseload.toStaffCase() = StaffCase(
    caseName = Name(
        forename = person.forename,
        middleName = listOfNotNull(person.secondName, person.thirdName).joinToString(" "),
        surname = person.surname
    ),
    crn = person.crn,
    staff = Name(forename = staff.forename, surname = staff.surname)
)

fun uk.gov.justice.digital.hmpps.integrations.delius.user.entity.Team.toTeam() = Team(
    description = description,
    cases = staff.flatMap { cl -> cl.caseLoad.map { it.toStaffCase() } })

fun uk.gov.justice.digital.hmpps.integrations.delius.user.entity.User.toUser() = User(
    cases = staff?.caseLoad?.map { it.toStaffCase() } ?: emptyList(),
    provider = staff?.provider?.description,
    teams = staff?.teams?.map { it.toTeam() } ?: emptyList())