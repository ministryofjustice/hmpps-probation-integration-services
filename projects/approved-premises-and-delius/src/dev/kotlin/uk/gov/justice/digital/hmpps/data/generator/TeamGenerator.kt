package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.ApprovedPremises
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team
import java.util.concurrent.atomic.AtomicLong

object TeamGenerator {
    private val teamCodeGenerator = AtomicLong(1)

    val APPROVED_PREMISES_TEAM = generate(ApprovedPremisesGenerator.DEFAULT)
    val APPROVED_PREMISES_TEAM_WITH_NO_STAFF = generate(ApprovedPremisesGenerator.NO_STAFF)
    val NON_APPROVED_PREMISES_TEAM = generate()

    fun generate(
        approvedPremises: ApprovedPremises? = null
    ) = Team(
        id = IdGenerator.getAndIncrement(),
        code = "TEST${teamCodeGenerator.getAndIncrement().toString().padStart(2, '0')}",
        approvedPremises = approvedPremises
    )
}
