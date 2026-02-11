package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Disposal
import uk.gov.justice.digital.hmpps.entity.DisposalType
import java.time.LocalDate

object DisposalGenerator {
    val DEFAULT_DISPOSAL = Disposal(
        id = IdGenerator.getAndIncrement(),
        disposalDate = LocalDate.now().minusDays(7),
        eventId = EventGenerator.DEFAULT_EVENT.eventId,
        length = 1,
        length2 = 2,
        disposalType = DisposalType(
            disposalTypeId = IdGenerator.getAndIncrement(),
            disposalTypeDescription = "Probation"
        ),
        lengthUnits = ReferenceDataGenerator.LENGTH_UNITS_MONTHS,
        length2Units = ReferenceDataGenerator.LENGTH_UNITS_DAYS
    )
}