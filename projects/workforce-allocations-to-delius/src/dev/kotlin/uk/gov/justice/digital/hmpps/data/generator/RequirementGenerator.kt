package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.Requirement
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person

object RequirementGenerator {
    val DEFAULT = generate()
    val NEW = generate(id = 9001)
    val HISTORIC = generate(id = 9002)

    fun generate(
        person: Person = PersonGenerator.DEFAULT,
        disposal: Disposal = DisposalGenerator.DEFAULT,
        id: Long = IdGenerator.getAndIncrement(),
        active: Boolean = true
    ) = Requirement(id, person, disposal, active)
}
