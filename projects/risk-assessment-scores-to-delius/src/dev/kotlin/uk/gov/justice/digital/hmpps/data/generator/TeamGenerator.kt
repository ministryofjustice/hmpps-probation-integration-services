package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Team

object TeamGenerator {
    val DEFAULT = generate("TEAM")
    fun generate(
        code: String,
        id: Long = IdGenerator.getAndIncrement()
    ) = Team(id, code)
}
