package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Exclusion
import uk.gov.justice.digital.hmpps.entity.LimitedAccessPerson
import uk.gov.justice.digital.hmpps.entity.LimitedAccessUser
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.Restriction

object LimitedAccessGenerator {
    val LAO_USER = LimitedAccessUser("DpsAndDelius", IdGenerator.getAndIncrement())

    val EXCLUDED_PERSON = LimitedAccessPerson(
        crn = "E000001",
        exclusionMessage = "This case is excluded",
        restrictionMessage = null,
        id = IdGenerator.getAndIncrement()
    )

    val RESTRICTED_PERSON = LimitedAccessPerson(
        crn = "R000001",
        exclusionMessage = null,
        restrictionMessage = "This case is restricted",
        id = IdGenerator.getAndIncrement()
    )

    val EXCLUDED_PERSON_RECORD = Person(
        id = EXCLUDED_PERSON.id,
        forename = "Excluded",
        secondName = null,
        thirdName = null,
        surname = "Person",
        crn = EXCLUDED_PERSON.crn,
        nomisId = "E0001AA",
        events = listOf(),
        softDeleted = false
    )

    val RESTRICTED_PERSON_RECORD = Person(
        id = RESTRICTED_PERSON.id,
        forename = "Restricted",
        secondName = null,
        thirdName = null,
        surname = "Person",
        crn = RESTRICTED_PERSON.crn,
        nomisId = "R0001AA",
        events = listOf(),
        softDeleted = false
    )

    val EXCLUSION = Exclusion(
        person = EXCLUDED_PERSON,
        user = LAO_USER,
        end = null,
        id = IdGenerator.getAndIncrement()
    )

    val RESTRICTION = Restriction(
        person = RESTRICTED_PERSON,
        user = LAO_USER,
        end = null,
        id = IdGenerator.getAndIncrement()
    )
}

