package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.*
import uk.gov.justice.digital.hmpps.set
import java.time.ZonedDateTime

object ProviderGenerator {

    val DEFAULT_PROVIDER = generateProvider("N03", "SWI")
    val DEFAULT_DISTRICT = generateDistrict("N03LDU1")
    val DEFAULT_TEAM = generateTeam("N03DEF", district = DEFAULT_DISTRICT)
    val POM_TEAM = generateTeam("N03POM", district = DEFAULT_DISTRICT)
    val UNALLOCATED_TEAM = generateTeam("N03ALL", district = DEFAULT_DISTRICT)
    val DEFAULT_STAFF = generateStaff("N03DEF0", "Default", "Staff")
    val UNALLOCATED_STAFF = generateStaff("N03ALLU", "Unallocated", "Staff")

    fun generateProvider(providerCode: String, prisonCode: String?, id: Long = IdGenerator.getAndIncrement()) =
        ProbationArea(
            id,
            providerCode,
            "Description of $providerCode",
            prisonCode?.let { Institution(IdGenerator.getAndIncrement(), it) }
        )

    fun generateDistrict(code: String, description: String = "LDU $code", id: Long = IdGenerator.getAndIncrement()) =
        District(code, description, id)

    fun generateTeam(
        code: String,
        description: String = "Team $code",
        district: District? = DEFAULT_DISTRICT,
        id: Long = IdGenerator.getAndIncrement()
    ) = Team(code, description, district, DEFAULT_PROVIDER, id)

    fun generateStaff(
        code: String,
        forename: String,
        surname: String,
        providerId: Long = DEFAULT_PROVIDER.id,
        middleName: String? = null,
        user: StaffUser? = null,
        startDate: ZonedDateTime = ZonedDateTime.now(),
    ) = Staff(code, forename, surname, middleName, user, providerId, startDate).apply { user?.set("staff", this) }
}
