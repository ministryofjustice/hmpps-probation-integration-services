package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.LocalAdminUnit
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team
import java.util.concurrent.atomic.AtomicLong

object TeamGenerator {
    private val teamCodeGenerator = AtomicLong(1)

    val APPROVED_PREMISES_TEAM_1 = generate(ProbationAreaGenerator.APPROVED_PREMISES_LAU_1)
    val APPROVED_PREMISES_TEAM_2 = generate(ProbationAreaGenerator.APPROVED_PREMISES_LAU_1)
    val APPROVED_PREMISES_TEAM_3 = generate(ProbationAreaGenerator.APPROVED_PREMISES_LAU_2)
    val NON_APPROVED_PREMISES_TEAM = generate(ProbationAreaGenerator.NON_APPROVED_PREMISES_LAU)

    fun generate(
        localAdminUnit: LocalAdminUnit
    ) = Team(
        id = IdGenerator.getAndIncrement(),
        code = "TEST${teamCodeGenerator.getAndIncrement().toString().padStart(2, '0')}",
        localAdminUnit = localAdminUnit
    )
}
