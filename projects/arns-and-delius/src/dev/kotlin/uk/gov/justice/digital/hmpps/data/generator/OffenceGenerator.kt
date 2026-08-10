package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Offence

object OffenceGenerator {
    val DEFAULT = generate(code = "00100", description = "Murder")
    val SECOND_OFFENCE = generate(code = "00101", description = "Second Offence")

    fun generate(
        code: String,
        description: String,
        id: Long = IdGenerator.getAndIncrement(),
    ) = Offence(
        id = id,
        code = code,
        description = description,
    )
}

