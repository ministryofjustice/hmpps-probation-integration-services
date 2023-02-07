package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import UserGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.LocalDeliveryUnit
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.StaffUser
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import uk.gov.justice.digital.hmpps.set

object ProviderGenerator {
    val DEFAULT_LDU = generateLdu("N03LDU1")
    val DEFAULT_TEAM = generateTeam("N03DEF", ldu = DEFAULT_LDU)
    val DEFAULT_STAFF = generateStaff("N03DEF0", "Default", "Staff", user = UserGenerator.DEFAULT_STAFF_USER)

    fun generateLdu(code: String, description: String = "LDU $code", id: Long = IdGenerator.getAndIncrement()) =
        LocalDeliveryUnit(code, description, id)

    fun generateTeam(
        code: String,
        description: String = "Team $code",
        ldu: LocalDeliveryUnit? = DEFAULT_LDU,
        id: Long = IdGenerator.getAndIncrement()
    ) = Team(code, description, ldu, id)

    fun generateStaff(
        code: String,
        forename: String,
        surname: String,
        middleName: String? = null,
        user: StaffUser? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Staff(code, forename, surname, middleName, user, id).apply { user?.set("staff", this) }
}
