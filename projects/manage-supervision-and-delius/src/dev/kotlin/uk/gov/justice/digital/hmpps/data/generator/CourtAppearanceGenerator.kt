package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.Court
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.CourtAppearance
import java.time.LocalDate

object CourtAppearanceGenerator {
    val DEFAULT_APPEARANCE_TYPE = ReferenceData(IdGenerator.getAndIncrement(), "S", "Default Appearance Type")

    val COURT_APPEARANCE = generate()
    fun generate(
        court: Court = CourtGenerator.DEFAULT,
        date: LocalDate = LocalDate.now().minusMonths(5),
        appearanceType: ReferenceData = DEFAULT_APPEARANCE_TYPE,
        id: Long = IdGenerator.getAndIncrement(),
        event: Event = PersonGenerator.EVENT_1
    ): CourtAppearance {
        return CourtAppearance(
            id,
            date,
            appearanceType,
            court,
            event
        )
    }
}
