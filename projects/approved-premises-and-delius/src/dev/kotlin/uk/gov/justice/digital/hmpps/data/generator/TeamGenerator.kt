package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.entity.ApprovedPremises
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team
import java.util.concurrent.atomic.AtomicLong

object TeamGenerator {
    private val teamCodeGenerator = AtomicLong(1)

    val APPROVED_PREMISES_TEAM = generate(ApprovedPremisesGenerator.DEFAULT)
    val APPROVED_PREMISES_TEAM_WITH_NO_STAFF = generate(ApprovedPremisesGenerator.NO_STAFF)
    val NON_APPROVED_PREMISES_TEAM = generate()
    val UNALLOCATED = generate(code = "N54UAT")

    fun generate(
        approvedPremises: ApprovedPremises? = null,
        code: String = "N54${teamCodeGenerator.getAndIncrement().toString().padStart(3, '0')}",
        description: String = "Description of Team $code"
    ) = Team(
        id = IdGenerator.getAndIncrement(),
        code = code,
        description = description,
        probationArea = ProbationAreaGenerator.DEFAULT,
        approvedPremises = approvedPremises
    )
}

object ProbationAreaGenerator {
    val DEFAULT = generate("N54")

    fun generate(code: String, id: Long = IdGenerator.getAndIncrement()) =
        ProbationArea(id, code)
}
