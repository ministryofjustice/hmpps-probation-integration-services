package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.*
import java.time.LocalDate
import java.time.ZoneId

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
        LocalDate.now().plusDays(1).atTime(12, 30).atZone(ZoneId.systemDefault()),
        IdGenerator.getAndIncrement(),
        DEFAULT_EVENT,
        DEFAULT_CA_TYPE,
        DEFAULT_COURT
    )

    val PERSON_2 = CourtAppearancePerson(
        IdGenerator.getAndIncrement(),
        "X012774"
    )
    val EVENT_2 = CourtAppearanceEventEntity(
        IdGenerator.getAndIncrement(),
        PERSON_2
    )
    val CA_2 = CourtAppearanceEntity(
        LocalDate.now().plusDays(1).atTime(12, 30).atZone(ZoneId.systemDefault()),
        IdGenerator.getAndIncrement(),
        EVENT_2,
        DEFAULT_CA_TYPE,
        DEFAULT_COURT
    )
    val CA_3 = CourtAppearanceEntity(
        LocalDate.of(2090, 1, 1).atStartOfDay(ZoneId.systemDefault()),
        IdGenerator.getAndIncrement(),
        EVENT_2,
        DEFAULT_CA_TYPE,
        DEFAULT_COURT
    )
}
