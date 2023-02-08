package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.common.entity.team.Team

object TeamGenerator {
    val DEFAULT = generate("TEAM")
    fun generate(
        code: String,
        id: Long = IdGenerator.getAndIncrement()
    ) = Team(id, code)
}
