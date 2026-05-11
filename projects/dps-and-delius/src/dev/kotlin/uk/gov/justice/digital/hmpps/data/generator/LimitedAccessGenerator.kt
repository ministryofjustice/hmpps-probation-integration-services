package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Exclusion
import uk.gov.justice.digital.hmpps.entity.LimitedAccessPerson
import uk.gov.justice.digital.hmpps.entity.LimitedAccessUser
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.Restriction

object LimitedAccessGenerator {
    val LAO_USER = LimitedAccessUser("DpsAndDelius", IdGenerator.getAndIncrement())

    val EXCLUDED_PERSON_RECORD = Person(
        id = IdGenerator.getAndIncrement(),
        forename = "Excluded",
        secondName = null,
        thirdName = null,
        surname = "Person",
        crn = "E000001",
        nomisId = "E0001AA",
        events = listOf(),
        softDeleted = false
    )

    val RESTRICTED_PERSON_RECORD = Person(
        id = IdGenerator.getAndIncrement(),
        forename = "Restricted",
        secondName = null,
        thirdName = null,
        surname = "Person",
        crn = "R000001",
        nomisId = "R0001AA",
        events = listOf(),
        softDeleted = false
    )

    private val EXCLUDED_LAO_PERSON = LimitedAccessPerson("E000001", "This case is excluded", null, EXCLUDED_PERSON_RECORD.id)
    private val RESTRICTED_LAO_PERSON = LimitedAccessPerson("R000001", null, "This case is restricted", RESTRICTED_PERSON_RECORD.id)

    val EXCLUSION = Exclusion(
        person = EXCLUDED_LAO_PERSON,
        user = LAO_USER,
        end = null,
        id = IdGenerator.getAndIncrement()
    )

    val RESTRICTION = Restriction(
        person = RESTRICTED_LAO_PERSON,
        user = LAO_USER,
        end = null,
        id = IdGenerator.getAndIncrement()
    )
}

