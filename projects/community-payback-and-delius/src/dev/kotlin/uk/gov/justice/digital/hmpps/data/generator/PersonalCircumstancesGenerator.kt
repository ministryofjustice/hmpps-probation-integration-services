package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.person.PersonalCircumstance
import uk.gov.justice.digital.hmpps.entity.person.PersonalCircumstanceSubType
import uk.gov.justice.digital.hmpps.entity.person.PersonalCircumstanceType
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

object PersonalCircumstancesGenerator{
    val person = PersonGenerator.DEFAULT_PERSON
    val TYPE = PersonalCircumstanceType(IdGenerator.getAndIncrement(), "TYPE1", "Type 1")
    val TYPE2 = PersonalCircumstanceType(IdGenerator.getAndIncrement(), "TYPE2", "Type 2")
    val TYPE3 = PersonalCircumstanceType(IdGenerator.getAndIncrement(), "TYPE3", "Type 3")
    val SUBTYPE = PersonalCircumstanceSubType(IdGenerator.getAndIncrement(), "SUB1", "Sub Type 1")
    val now = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS)

    val PERSONALCIRCUMSTANCE1 =
        PersonalCircumstance(
            id = IdGenerator.getAndIncrement(),
            person = person,
            type = TYPE,
            subType = SUBTYPE,
            startDate = now.minusDays(10),
            endDate = null
        )

    val PERSONALCIRCUMSTANCE2 =
        PersonalCircumstance(
            id = IdGenerator.getAndIncrement(),
            person = person,
            type = TYPE2,
            subType = null,
            startDate = now.minusDays(2),
            endDate = now.plusDays(5)
        )

    val PERSONALCIRCUMSTANCE3 =
        PersonalCircumstance(
            id = IdGenerator.getAndIncrement(),
            person = person,
            type = TYPE3,
            subType = null,
            startDate = now.minusDays(20),
            endDate = now.minusDays(1)
        )
}