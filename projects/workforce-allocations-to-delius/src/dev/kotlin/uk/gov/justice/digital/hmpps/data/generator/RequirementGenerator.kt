package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.Requirement
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person

object RequirementGenerator {
    val DEFAULT = generate()

    fun generate(
        person: Person = PersonGenerator.DEFAULT,
        id: Long = IdGenerator.getAndIncrement(),
        active: Boolean = true
    ) = Requirement(id, person, active)
}
