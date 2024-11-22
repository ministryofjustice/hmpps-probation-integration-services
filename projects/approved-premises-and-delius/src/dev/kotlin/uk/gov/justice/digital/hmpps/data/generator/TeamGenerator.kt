package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.entity.ApprovedPremises
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team
import java.util.concurrent.atomic.AtomicLong

object TeamGenerator {
    private val teamCodeGenerator = AtomicLong(1)

    val AP_TEAM_LDU = ProbationCaseGenerator.generateLdu("N54LDU")
    val APPROVED_PREMISES_TEAM = generate(ApprovedPremisesGenerator.DEFAULT)
    val NON_APPROVED_PREMISES_TEAM = generate()
    val UNALLOCATED = generate(code = "N54UAT")

    val ALL_AP_TEAMS = listOf(
        APPROVED_PREMISES_TEAM,
        generate(ApprovedPremisesGenerator.AP_Q005),
        generate(ApprovedPremisesGenerator.AP_Q049),
        generate(ApprovedPremisesGenerator.AP_Q095),
        generate(ApprovedPremisesGenerator.AP_Q701),
        generate(ApprovedPremisesGenerator.AP_Q702),
        generate(ApprovedPremisesGenerator.AP_Q703),
        generate(ApprovedPremisesGenerator.AP_Q704),
        generate(ApprovedPremisesGenerator.AP_Q705),
        generate(ApprovedPremisesGenerator.AP_Q706),
        generate(ApprovedPremisesGenerator.AP_Q707),
        generate(ApprovedPremisesGenerator.AP_Q708),
        generate(ApprovedPremisesGenerator.AP_Q709),
        generate(ApprovedPremisesGenerator.AP_Q710),
        generate(ApprovedPremisesGenerator.AP_Q711),
        generate(ApprovedPremisesGenerator.AP_Q712),
        generate(ApprovedPremisesGenerator.AP_Q713),
        generate(ApprovedPremisesGenerator.AP_Q714),
        generate(ApprovedPremisesGenerator.AP_Q715),
        generate(ApprovedPremisesGenerator.AP_Q716)
    )

    val ALL_TEAMS = listOf(
        NON_APPROVED_PREMISES_TEAM,
        UNALLOCATED
    ) + ALL_AP_TEAMS

    fun generate(
        approvedPremises: ApprovedPremises? = null,
        code: String = "N54${teamCodeGenerator.getAndIncrement().toString().padStart(3, '0')}",
    ) = Team(
        id = IdGenerator.getAndIncrement(),
        code = code,
        description = "Description of Team $code",
        probationArea = ProbationAreaGenerator.DEFAULT,
        approvedPremises = approvedPremises,
        district = AP_TEAM_LDU
    )
}

object ProbationAreaGenerator {
    val DEFAULT = generate(code = "N54", description = "A description")
    val N58_SW = generate(code = "N58", description = "SW")

    fun generate(code: String, id: Long = IdGenerator.getAndIncrement(), description: String) =
        ProbationArea(id, code, description)
}
