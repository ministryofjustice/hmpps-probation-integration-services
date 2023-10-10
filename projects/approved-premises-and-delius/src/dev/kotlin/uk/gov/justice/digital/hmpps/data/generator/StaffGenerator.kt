package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.STAFF_GRADE
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.entity.ApprovedPremises
import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffUser
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team
import uk.gov.justice.digital.hmpps.set
import java.util.concurrent.atomic.AtomicLong

object StaffGenerator {
    private val staffCodeGenerator = AtomicLong(1)
    val DEFAULT_STAFF = generate()

    val DEFAULT_STAFF_USER = generateStaffUser("john-smith", DEFAULT_STAFF)

    fun generate(
        name: String = "TEST",
        code: String = "TEST${staffCodeGenerator.getAndIncrement().toString().padStart(3, '0')}",
        teams: List<Team> = listOf(),
        approvedPremises: List<ApprovedPremises> = listOf()
    ) = Staff(
        id = IdGenerator.getAndIncrement(),
        code = code,
        grade = STAFF_GRADE,
        forename = "Test",
        middleName = null,
        surname = name,
        teams = teams,
        approvedPremises = approvedPremises
    )

    fun generateStaffUser(
        username: String,
        staff: Staff? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = StaffUser(staff, username, id).apply { staff?.set("user", this) }
}
