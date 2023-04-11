package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.controller.entity.Disposal
import uk.gov.justice.digital.hmpps.controller.entity.DisposalType
import uk.gov.justice.digital.hmpps.controller.entity.EventEntity

object DisposalGenerator {
    val DEFAULT = generate()

    fun generate(
        event: EventEntity = EventGenerator.DEFAULT,
        id: Long = IdGenerator.getAndIncrement(),
        type: DisposalType = DisposalTypeGenerator.DEFAULT,
    ) = Disposal(
        id,
        event,
        type,
        listOf(),
    )
}
