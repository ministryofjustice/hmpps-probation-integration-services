package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceData
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
    val FUTURE_POM_STAFF = generateStaff("N03POMF", "Future", "POM")
    val FUTURE_POM = generatePom(PersonGenerator.DEFAULT.id, ZonedDateTime.now().plusDays(3))
    val FUTURE_RO = generateResponsibleOfficer(PersonGenerator.DEFAULT.id, FUTURE_POM, ZonedDateTime.now().plusDays(3))

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

    fun generatePom(
        personId: Long,
        date: ZonedDateTime,
        allocationReason: ReferenceData = ReferenceDataGenerator.ALLOCATION_AUTO,
        staff: Staff = FUTURE_POM_STAFF,
        team: Team = POM_TEAM,
        probationArea: ProbationArea = DEFAULT_PROVIDER,
    ) = PrisonManager(0, 0, personId, date, allocationReason, staff, team, probationArea)

    fun generateResponsibleOfficer(
        personId: Long,
        prisonManager: PrisonManager,
        startDate: ZonedDateTime,
    ) = ResponsibleOfficer(personId, prisonManager, startDate)
}
