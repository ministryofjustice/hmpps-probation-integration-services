package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.entity.ApprovedPremises
import uk.gov.justice.digital.hmpps.integrations.delius.person.Ldu
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team
import java.util.concurrent.atomic.AtomicLong

object TeamGenerator {
    private val teamCodeGenerator = AtomicLong(1)

    val AP_TEAM_LDU = ProbationCaseGenerator.generateLdu("N54LDU")
    val APPROVED_PREMISES_TEAM = generate(ApprovedPremisesGenerator.DEFAULT)
    val APPROVED_PREMISES_TEAM_WITH_NO_STAFF = generate(ApprovedPremisesGenerator.NO_STAFF)
    val NON_APPROVED_PREMISES_TEAM = generate()
    val UNALLOCATED = generate(code = "N54UAT")
    val E2E_TEST_TEAM = generate(ApprovedPremisesGenerator.E2E_TEST)

    fun generate(
        approvedPremises: ApprovedPremises? = null,
        code: String = "N54${teamCodeGenerator.getAndIncrement().toString().padStart(3, '0')}",
        description: String = "Description of Team $code",
        district: Ldu = AP_TEAM_LDU
    ) = Team(
        id = IdGenerator.getAndIncrement(),
        code = code,
        description = description,
        probationArea = ProbationAreaGenerator.DEFAULT,
        approvedPremises = approvedPremises,
        district = district
    )
}

object ProbationAreaGenerator {
    val DEFAULT = generate(code = "N54", description = "A description")

    fun generate(code: String, id: Long = IdGenerator.getAndIncrement(), description: String) =
        ProbationArea(id, code, description)
}
