package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.*
import java.time.LocalDateTime

object LimitedAccessGenerator {
    val EXCLUSION = generateExclusion(PersonGenerator.EXCLUSION)
    val RESTRICTION =
        generateRestriction(PersonGenerator.RESTRICTION, endDateTime = LocalDateTime.now().plusDays(1))
    val BOTH_EXCLUSION = generateExclusion(
        PersonGenerator.RESTRICTION_EXCLUSION,
        LimitedAccessUserGenerator.RESTRICTION_AND_EXCLUSION_USER
    )
    val BOTH_RESTRICTION = generateRestriction(
        PersonGenerator.RESTRICTION_EXCLUSION,
        LimitedAccessUserGenerator.RESTRICTION_AND_EXCLUSION_USER
    )

    fun generateExclusion(
        person: Person,
        user: LimitedAccessUser = LimitedAccessUserGenerator.EXCLUSION_USER,
        endDateTime: LocalDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Exclusion(person.limitedAccess(), user.limitedAccess(), endDateTime, id)

    fun generateRestriction(
        person: Person,
        user: LimitedAccessUser = LimitedAccessUserGenerator.RESTRICTION_USER,
        endDateTime: LocalDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Restriction(person.limitedAccess(), user.limitedAccess(), endDateTime, id)

    private fun Person.limitedAccess() = LimitedAccessPerson(crn, exclusionMessage, restrictionMessage, id)
    private fun LimitedAccessUser.limitedAccess() = LimitedAccessUser(username, id)
}
