package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.STAFF_GRADE
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.entity.ApprovedPremises
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffUser
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team
import uk.gov.justice.digital.hmpps.set
import java.util.concurrent.atomic.AtomicLong

object StaffGenerator {
    private val staffCodeGenerator = AtomicLong(1)
    val DEFAULT_STAFF = generate(teams = listOf(TeamGenerator.NON_APPROVED_PREMISES_TEAM))
    val JIM_SNOW = generate(
        name = "Jim Snow",
        probationArea = ProbationAreaGenerator.N58_SW
    )
    val LAO_FULL_ACCESS = generate(
        name = "LAO Full Access"
    )
    val LAO_RESTRICTED = generate(
        name = "LAO Restricted"
    )
    val CRU_WOMENS_ESTATE = generate(
        name = "CRU Womens Estate"
    )

    val DEFAULT_STAFF_USER = generateStaffUser("john-smith", DEFAULT_STAFF)
    val JIM_SNOW_USER = generateStaffUser("JIMSNOWLDAP", JIM_SNOW)
    val LAO_FULL_ACCESS_USER = generateStaffUser("LAOFULLACCESS", LAO_FULL_ACCESS)
    val LAO_RESTRICTED_USER = generateStaffUser("LAORESTRICTED", LAO_RESTRICTED)
    val CRU_WOMENS_ESTATE_USER = generateStaffUser("CRUWOMENSESTATE", CRU_WOMENS_ESTATE)

    fun generate(
        name: String = "Test",
        code: String = "TEST${staffCodeGenerator.getAndIncrement().toString().padStart(3, '0')}",
        teams: List<Team> = listOf(),
        approvedPremises: List<ApprovedPremises> = listOf(),
        probationArea: ProbationArea = ProbationAreaGenerator.DEFAULT
    ) = Staff(
        id = IdGenerator.getAndIncrement(),
        code = code,
        grade = STAFF_GRADE,
        forename = "Test",
        middleName = null,
        surname = name,
        teams = teams,
        approvedPremises = approvedPremises,
        probationArea = probationArea
    )

    fun generateStaffUser(
        username: String,
        staff: Staff? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = StaffUser(staff, username, id).apply { staff?.set("user", this) }
}
