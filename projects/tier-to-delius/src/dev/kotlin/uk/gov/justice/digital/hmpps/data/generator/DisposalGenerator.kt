package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.DisposalType
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventEntity
import uk.gov.justice.digital.hmpps.integrations.delius.requirement.RequirementEntity

object DisposalGenerator {
    val DEFAULT = generate()

    fun generate(
        event: EventEntity = EventGenerator.DEFAULT,
        id: Long = IdGenerator.getAndIncrement(),
        type: DisposalType = DisposalTypeGenerator.DEFAULT,
        custody: Custody? = null,
        requirements: List<RequirementEntity> = listOf()
    ) = Disposal(
        id,
        event,
        type,
        custody,
        requirements
    )
}
