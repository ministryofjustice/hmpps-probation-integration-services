package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.event.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.event.DisposalType
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event

object DisposalGenerator {
    val DEFAULT = generate()

    fun generate(
        event: Event = EventGenerator.DEFAULT,
        id: Long = IdGenerator.getAndIncrement(),
        active: Boolean = true
    ) = Disposal(id, DisposalType(IdGenerator.getAndIncrement(), "SC"), event, active = active)
}
