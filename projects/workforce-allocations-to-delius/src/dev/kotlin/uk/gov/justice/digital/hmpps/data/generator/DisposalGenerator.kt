package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.DisposalType
import java.time.ZonedDateTime

object DisposalGenerator {
    val DEFAULT = generate()
    val INACTIVE = generate(EventGenerator.INACTIVE, type = DEFAULT.type, active = false)

    fun generate(
        event: Event = EventGenerator.DEFAULT,
        id: Long = IdGenerator.getAndIncrement(),
        type: DisposalType = DisposalType(IdGenerator.getAndIncrement(), "SC", "Sentenced - In Custody"),
        entryLength: Long = 12,
        entryLengthUnit: ReferenceData = ReferenceDataGenerator.UNIT_MONTHS,
        active: Boolean = true
    ) = Disposal(
        id,
        type,
        event,
        active = active,
        entryLength = entryLength,
        entryLengthUnit = entryLengthUnit,
        date = ZonedDateTime.now().minusDays(2)
    )
}
