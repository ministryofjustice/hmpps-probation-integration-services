package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.Court
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.CourtAppearance
import java.time.LocalDate

object CourtAppearanceGenerator {

    val COURT_APPEARANCE = generate()
    fun generate(
        court: Court = CourtGenerator.DEFAULT,
        date: LocalDate = LocalDate.now().minusMonths(5),
        id: Long = IdGenerator.getAndIncrement(),
        event: Event = PersonGenerator.EVENT_1
    ): CourtAppearance {
        return CourtAppearance(
            id,
            date,
            court,
            event
        )
    }
}
