package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.CourtAppearance
import uk.gov.justice.digital.hmpps.entity.CourtEntity
import java.time.LocalDate

object CourtAppearanceGenerator {
    val DEFAULT_COURT_APPEARANCE = CourtAppearance(
        id = IdGenerator.getAndIncrement(),
        eventId = EventGenerator.DEFAULT_EVENT.eventId,
        court = getCourt("Warwick Magistrates Court"),
        appearanceType = ReferenceDataGenerator.SENTENCE_APPEARANCE_TYPE,
        outcome = ReferenceDataGenerator.DEFAULT_OUTCOME,
        softDeleted = false
    )

    val MISSING_DISPOSAL_COURT_APPEARANCE = CourtAppearance(
        id = IdGenerator.getAndIncrement(),
        eventId = EventGenerator.MISSING_DISPOSAL_EVENT.eventId,
        court = getCourt("Birmingham Magistrates Court"),
        appearanceType = ReferenceDataGenerator.SENTENCE_APPEARANCE_TYPE,
        outcome = ReferenceDataGenerator.DEFAULT_OUTCOME,
        softDeleted = false
    )

    fun getCourt(description: String) = CourtEntity(
        id = IdGenerator.getAndIncrement(),
        appearanceDate = LocalDate.now().minusDays(7),
        courtName = description
    )
}