package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonManager

object PersonManagerGenerator {
    val DEFAULT = generate()
    fun generate(
        person: Person = PersonGenerator.DEFAULT,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonManager(
        id,
        person.id,
        TeamGenerator.DEFAULT,
        StaffGenerator.DEFAULT
    )
}
