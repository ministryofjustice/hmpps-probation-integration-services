package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team

object TeamGenerator {
    val DEFAULT = generate(
        "${ProbationAreaGenerator.DEFAULT.code}UAT",
        "Unallocated Team(N02)"
    )

    fun allStaff(
        probationArea: ProbationArea = ProbationAreaGenerator.DEFAULT,
    ) = generate("${probationArea.code}ALL", probationArea = probationArea)

    fun generate(
        code: String,
        description: String = code,
        probationArea: ProbationArea = ProbationAreaGenerator.DEFAULT,
        id: Long = IdGenerator.getAndIncrement()
    ) = Team(id, code, description, probationArea)
}
