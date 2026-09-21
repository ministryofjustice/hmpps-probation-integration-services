package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Exclusion
import uk.gov.justice.digital.hmpps.entity.LimitedAccessPerson
import uk.gov.justice.digital.hmpps.entity.LimitedAccessUser
import uk.gov.justice.digital.hmpps.entity.Restriction
import java.time.ZonedDateTime

object LimitedAccessGenerator {

    val EXCLUSION = generateExclusion(
        person = PersonGenerator.EXCLUDED,
        user = UserGenerator.DEFAULT
    )

    val RESTRICTION = generateRestriction(
        person = PersonGenerator.RESTRICTED,
        user = UserGenerator.RESTRICTED
    )

    val BOTH_EXCLUSION = generateExclusion(
        person = PersonGenerator.BOTH,
        user = UserGenerator.DEFAULT
    )

    val BOTH_RESTRICTION = generateRestriction(
        person = PersonGenerator.BOTH,
        user = UserGenerator.RESTRICTED
    )

    fun generateExclusion(
        person: LimitedAccessPerson,
        user: LimitedAccessUser,
        start: ZonedDateTime = ZonedDateTime.now().minusDays(1),
        endDateTime: ZonedDateTime? = null,
        id: Long = IdGenerator.getAndIncrement(),
        createdDateTime: ZonedDateTime = ZonedDateTime.now().minusDays(1),
        lastUpdatedDateTime: ZonedDateTime? = null,
    ) = Exclusion(person, user, start, endDateTime, id, createdDateTime, lastUpdatedDateTime)

    fun generateRestriction(
        person: LimitedAccessPerson,
        user: LimitedAccessUser,
        start: ZonedDateTime = ZonedDateTime.now().minusDays(1),
        endDateTime: ZonedDateTime? = null,
        id: Long = IdGenerator.getAndIncrement(),
        createdDateTime: ZonedDateTime = ZonedDateTime.now().minusDays(1),
        lastUpdatedDateTime: ZonedDateTime? = null,
    ) = Restriction(person, user, start, endDateTime, id, createdDateTime, lastUpdatedDateTime)
}