package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.entity.Court
import uk.gov.justice.digital.hmpps.entity.CourtAppearanceEntity
import uk.gov.justice.digital.hmpps.entity.CourtAppearanceEventEntity
import uk.gov.justice.digital.hmpps.entity.CourtAppearancePerson
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import java.time.ZonedDateTime

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
    val DEFAULT_CA = CourtAppearanceEntity(
        ZonedDateTime.now(),
        IdGenerator.getAndIncrement(),
        DEFAULT_EVENT,
        DEFAULT_CA_TYPE,
        DEFAULT_COURT
    )
}
