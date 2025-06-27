package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.DisposalType
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventEntity

object DisposalGenerator {
    val DEFAULT = generate()

    fun generate(
        event: EventEntity = EventGenerator.DEFAULT,
        id: Long = IdGenerator.getAndIncrement(),
        type: DisposalType = DisposalTypeGenerator.DEFAULT
    ) = Disposal(
        id,
        event,
        type,
        listOf()
    )
}
