package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.DisposalType
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventEntity
import uk.gov.justice.digital.hmpps.integrations.delius.requirement.RequirementEntity
import java.time.LocalDate

object DisposalGenerator {
    val DEFAULT = generate()
    val RECALLED = generate(event = EventGenerator.RECALLED, startDate = LocalDate.of(2008, 1, 1))

    fun generate(
        event: EventEntity = EventGenerator.DEFAULT,
        id: Long = IdGenerator.getAndIncrement(),
        type: DisposalType = DisposalTypeGenerator.DEFAULT,
        custody: Custody? = null,
        requirements: List<RequirementEntity> = listOf(),
        startDate: LocalDate = LocalDate.of(2009, 1, 1)
    ) = Disposal(
        id = id,
        eventEntity = event,
        disposalType = type,
        custody = custody,
        requirements = requirements,
        startDate = startDate
    )
}
