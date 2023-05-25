package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Offence

object OffenceGenerator {
    val DEFAULT = generate()

    fun generate(
        id: Long = IdGenerator.getAndIncrement()
    ) = Offence(id, "Arson")
}
