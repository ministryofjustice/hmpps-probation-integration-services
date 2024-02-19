package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.team.Team

object TeamGenerator {
    val DEFAULT = generate("${ProbationAreaGenerator.DEFAULT.code}UTS")
    fun generate(
        code: String,
        id: Long = IdGenerator.getAndIncrement()
    ) = Team(id, code)
}
