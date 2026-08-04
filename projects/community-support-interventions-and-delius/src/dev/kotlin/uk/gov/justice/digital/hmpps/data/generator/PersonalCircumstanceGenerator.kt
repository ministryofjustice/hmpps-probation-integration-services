package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.PersonalCircumstance
import uk.gov.justice.digital.hmpps.entity.PersonalCircumstanceSubType
import uk.gov.justice.digital.hmpps.entity.PersonalCircumstanceType
import java.time.LocalDate
import java.time.ZonedDateTime

object PersonalCircumstanceGenerator {
    val TYPE_EMPLOYMENT = PersonalCircumstanceType(
        id = IdGenerator.getAndIncrement(),
        code = "EMP",
        description = "Employment",
    )

    val SUB_TYPE_FULL_TIME = PersonalCircumstanceSubType(
        id = IdGenerator.getAndIncrement(),
        description = "Full-time employed (30+ hours)",
    )

    val CIRCUMSTANCE = PersonalCircumstance(
        id = IdGenerator.getAndIncrement(),
        personId = PersonGenerator.PERSON1.id,
        type = TYPE_EMPLOYMENT,
        subType = SUB_TYPE_FULL_TIME,
        lastUpdated = ZonedDateTime.parse("2024-05-20T14:00:00+01:00"),
        startDate = LocalDate.of(2024, 1, 1),
        softDeleted = false,
    )
}

