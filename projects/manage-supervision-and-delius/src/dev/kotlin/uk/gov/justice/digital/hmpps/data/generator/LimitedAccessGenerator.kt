package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator
import uk.gov.justice.digital.hmpps.entity.Exclusion
import uk.gov.justice.digital.hmpps.entity.LimitedAccessPerson
import uk.gov.justice.digital.hmpps.entity.LimitedAccessUser
import uk.gov.justice.digital.hmpps.entity.Restriction
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.User
import java.time.LocalDateTime

object LimitedAccessGenerator {
    val EXCLUSION = generateExclusion(PersonDetailsGenerator.EXCLUSION)
    val RESTRICTION =
        generateRestriction(PersonDetailsGenerator.RESTRICTION, endDateTime = LocalDateTime.now().plusHours(1))
    val BOTH_EXCLUSION = generateExclusion(PersonDetailsGenerator.RESTRICTION_EXCLUSION)
    val BOTH_RESTRICTION = generateRestriction(
        PersonDetailsGenerator.RESTRICTION_EXCLUSION,
        endDateTime = LocalDateTime.now().plusHours(1)
    )

    fun generateExclusion(
        person: Person,
        user: User = ContactGenerator.LIMITED_ACCESS_USER,
        endDateTime: LocalDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Exclusion(person.limitedAccess(), user.limitedAccess(), endDateTime, id)

    fun generateRestriction(
        person: Person,
        user: User = ContactGenerator.USER_1,
        endDateTime: LocalDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Restriction(person.limitedAccess(), user.limitedAccess(), endDateTime, id)

    private fun Person.limitedAccess() = LimitedAccessPerson(crn, exclusionMessage, restrictionMessage, id)
    private fun User.limitedAccess() = LimitedAccessUser(username, id)
}
