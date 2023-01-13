package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.STAFF_GRADE
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.ApprovedPremises
import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team
import java.util.concurrent.atomic.AtomicLong

object StaffGenerator {
    private val staffCodeGenerator = AtomicLong(1)

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
}
