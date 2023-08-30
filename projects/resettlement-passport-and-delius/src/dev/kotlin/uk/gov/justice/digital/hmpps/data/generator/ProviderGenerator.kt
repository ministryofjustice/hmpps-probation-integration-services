package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.ProbationArea
import uk.gov.justice.digital.hmpps.entity.Staff
import uk.gov.justice.digital.hmpps.entity.StaffUser
import uk.gov.justice.digital.hmpps.entity.Team

object ProviderGenerator {
    val DEFAULT_AREA = generateProbationArea()
    val DEFAULT_TEAM = generateTeam("N03DEF")
    val DEFAULT_STAFF = generateStaff("N03DEF1", "John", "Smith", "James")
    val DEFAULT_STAFF_USER = generateStaffUser("john-smith", DEFAULT_STAFF)

    fun generateProbationArea(
        id: Long = IdGenerator.getAndIncrement(),
        code: String = "LDN",
        description: String = "London"
    ) = ProbationArea(id, code, description)

    fun generateTeam(
        code: String,
        description: String = "Team $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = Team(id, code, description)

    fun generateStaff(
        code: String,
        forename: String,
        surname: String,
        middleName: String? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Staff(code, forename, surname, middleName, null, id)

    fun generateStaffUser(
        username: String,
        staff: Staff,
        id: Long = IdGenerator.getAndIncrement()
    ) = StaffUser(username, staff, id)
}
