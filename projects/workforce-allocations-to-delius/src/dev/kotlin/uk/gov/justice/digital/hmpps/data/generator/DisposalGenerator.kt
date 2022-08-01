package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.Disposal

object DisposalGenerator {
    val DEFAULT = generate()

    fun generate(
        event: Event = EventGenerator.DEFAULT,
        id: Long = IdGenerator.getAndIncrement(),
        active: Boolean = true
    ) = Disposal(id, event, active)
}
