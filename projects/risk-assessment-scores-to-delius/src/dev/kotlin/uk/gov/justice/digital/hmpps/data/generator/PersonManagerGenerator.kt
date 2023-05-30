package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonManager

object PersonManagerGenerator {
    val DEFAULT = generate()
    fun generate(
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonManager(
        id,
        PersonGenerator.DEFAULT.id,
        TeamGenerator.DEFAULT,
        StaffGenerator.DEFAULT
    )
}
