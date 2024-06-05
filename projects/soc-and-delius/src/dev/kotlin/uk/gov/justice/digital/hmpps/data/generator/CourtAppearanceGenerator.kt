package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.*
import java.time.LocalDate

object CourtAppearanceGenerator {
    val DEFAULT_PERSON = CourtAppearancePerson(
        IdGenerator.getAndIncrement(),
        "X012771"
    )
    val DEFAULT_EVENT = CourtAppearanceEventEntity(
        IdGenerator.getAndIncrement(),
        DEFAULT_PERSON
    )
    val DEFAULT_COURT = Court(
        IdGenerator.getAndIncrement(),
        "AYLSYC",
        "Aylesbury Youth Court"
    )
    val DEFAULT_CA_TYPE = ReferenceData(
        IdGenerator.getAndIncrement(),
        "T",
        "Trial/Adjournment"
    )
    val DEFAULT_OUTCOME = ReferenceData(
        IdGenerator.getAndIncrement(),
        "505",
        "Breach - Continued/Fine"
    )
    val DEFAULT_CA = CourtAppearanceEntity(
        LocalDate.now(),
        IdGenerator.getAndIncrement(),
        DEFAULT_EVENT,
        DEFAULT_CA_TYPE,
        DEFAULT_COURT
    )
}
