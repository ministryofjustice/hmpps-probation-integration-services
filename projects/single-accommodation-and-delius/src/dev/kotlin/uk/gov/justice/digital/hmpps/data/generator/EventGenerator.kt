package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.event.Custody
import uk.gov.justice.digital.hmpps.entity.event.Disposal
import uk.gov.justice.digital.hmpps.entity.event.Event
import uk.gov.justice.digital.hmpps.entity.event.KeyDate
import java.time.LocalDate

object EventGenerator {
    val DEFAULT = Event(
        id = IdGenerator.getAndIncrement(),
        personId = PersonGenerator.DEFAULT.id
    )
}

object DisposalGenerator {
    val DEFAULT = Disposal(
        id = IdGenerator.getAndIncrement(),
        event = EventGenerator.DEFAULT
    )
}

object CustodyGenerator {
    val DEFAULT = Custody(
        id = IdGenerator.getAndIncrement(),
        disposal = DisposalGenerator.DEFAULT
    )
}

object KeyDateGenerator {
    val EXPECTED_RELEASE = KeyDate(
        id = IdGenerator.getAndIncrement(),
        custody = CustodyGenerator.DEFAULT,
        type = ReferenceDataGenerator.EXP_RELEASE_DATE_TYPE,
        date = LocalDate.of(2026, 6, 1)
    )
}
