package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.ApprovedPremises
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team
import java.util.concurrent.atomic.AtomicLong

object StaffGenerator {
    val STAFF_GRADE = ReferenceData(IdGenerator.getAndIncrement(), "TEST", "Test staff grade")
    private val staffCodeGenerator = AtomicLong(1)

    fun generate(
        name: String,
        teams: List<Team>,
        approvedPremises: List<ApprovedPremises>
    ) = Staff(
        id = IdGenerator.getAndIncrement(),
        code = "TEST${staffCodeGenerator.getAndIncrement().toString().padStart(3, '0')}",
        grade = STAFF_GRADE,
        forename = "Test",
        middleName = null,
        surname = name,
        teams = teams,
        approvedPremises = approvedPremises
    )
}
