package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.District
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.StaffUser
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import uk.gov.justice.digital.hmpps.set

object ProviderGenerator {
    val DEFAULT_DISTRICT = generateDistrict("N03LDU1")
    val DEFAULT_TEAM = generateTeam("N03DEF", district = DEFAULT_DISTRICT)
    val DEFAULT_STAFF = generateStaff("N03DEF0", "Default", "Staff", user = UserGenerator.DEFAULT_STAFF_USER)

    fun generateDistrict(code: String, description: String = "LDU $code", id: Long = IdGenerator.getAndIncrement()) =
        District(code, description, id)

    fun generateTeam(
        code: String,
        description: String = "Team $code",
        district: District? = DEFAULT_DISTRICT,
        id: Long = IdGenerator.getAndIncrement()
    ) = Team(code, description, district, id)

    fun generateStaff(
        code: String,
        forename: String,
        surname: String,
        middleName: String? = null,
        user: StaffUser? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Staff(code, forename, surname, middleName, user, id).apply { user?.set("staff", this) }
}
