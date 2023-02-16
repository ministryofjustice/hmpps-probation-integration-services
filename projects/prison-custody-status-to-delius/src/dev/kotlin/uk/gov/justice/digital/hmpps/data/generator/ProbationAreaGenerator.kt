package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea

object ProbationAreaGenerator {
    val DEFAULT = generate("N02", "NPS North East")

    fun generate(
        code: String,
        description: String = "description for $code"
    ) = ProbationArea(IdGenerator.getAndIncrement(), code, description)
}
