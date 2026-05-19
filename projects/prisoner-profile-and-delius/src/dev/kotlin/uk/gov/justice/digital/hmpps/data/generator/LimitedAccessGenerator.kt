package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Exclusion
import uk.gov.justice.digital.hmpps.entity.LimitedAccessPerson
import uk.gov.justice.digital.hmpps.entity.LimitedAccessUser
import uk.gov.justice.digital.hmpps.entity.Restriction
import uk.gov.justice.digital.hmpps.integrations.delius.documents.entity.DocumentEntity
import uk.gov.justice.digital.hmpps.integrations.delius.documents.entity.Person
import java.time.ZonedDateTime

object LimitedAccessGenerator {
    val LAO_EXCLUDED_PERSON = Person(
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

    val LAO_RESTRICTED_PERSON = Person(
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

    val EXCLUDED_LAO_USER = LimitedAccessUser("lao-excluded-user", IdGenerator.getAndIncrement())
    val RESTRICTED_LAO_USER = LimitedAccessUser("lao-restricted-user", IdGenerator.getAndIncrement())

    val EXCLUDED_LIMITED_ACCESS_PERSON = LimitedAccessPerson(
        crn = LAO_EXCLUDED_PERSON.crn,
        exclusionMessage = "You are excluded from this case",
        restrictionMessage = null,
        id = LAO_EXCLUDED_PERSON.id
    )

    val RESTRICTED_LIMITED_ACCESS_PERSON = LimitedAccessPerson(
        crn = LAO_RESTRICTED_PERSON.crn,
        exclusionMessage = null,
        restrictionMessage = "This case is restricted",
        id = LAO_RESTRICTED_PERSON.id
    )

    val LAO_EXCLUSION = Exclusion(
        person = EXCLUDED_LIMITED_ACCESS_PERSON,
        user = EXCLUDED_LAO_USER,
        end = null,
        id = IdGenerator.getAndIncrement()
    )

    val LAO_RESTRICTION = Restriction(
        person = RESTRICTED_LIMITED_ACCESS_PERSON,
        user = RESTRICTED_LAO_USER,
        end = null,
        id = IdGenerator.getAndIncrement()
    )

    val LAO_EXCLUDED_PERSON_DOCUMENT = DocumentEntity(
        id = IdGenerator.getAndIncrement(),
        personId = LAO_EXCLUDED_PERSON.id,
        alfrescoId = "00000000-0000-0000-0000-000000000099",
        primaryKeyId = LAO_EXCLUDED_PERSON.id,
        name = "OFFENDER-related document",
        type = "DOCUMENT",
        tableName = "OFFENDER",
        createdAt = ZonedDateTime.now(),
        createdByUserId = 0,
        lastUpdatedUserId = 0,
        softDeleted = false
    )
}
