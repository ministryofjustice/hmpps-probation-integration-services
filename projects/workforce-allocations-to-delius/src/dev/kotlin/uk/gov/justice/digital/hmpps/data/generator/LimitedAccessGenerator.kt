package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Exclusion
import uk.gov.justice.digital.hmpps.entity.LimitedAccessPerson
import uk.gov.justice.digital.hmpps.entity.LimitedAccessUser
import uk.gov.justice.digital.hmpps.entity.Restriction
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffWithUser
import uk.gov.justice.digital.hmpps.user.AuditUser
import java.time.ZonedDateTime

object LimitedAccessGenerator {
    val EXCLUSION = generateExclusion()
    val RESTRICTION = generateRestriction(endDateTime = ZonedDateTime.now().plusHours(1))

    fun generateExclusion(
        user: LimitedAccessUser = UserGenerator.LIMITED_ACCESS_USER.limitedAccess(),
        person: Person = PersonGenerator.EXCLUSION,
        start: ZonedDateTime = ZonedDateTime.now(),
        endDateTime: ZonedDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Exclusion(person.limitedAccess(), user, start, endDateTime, id)

    fun generateRestriction(
        user: LimitedAccessUser = StaffGenerator.STAFF_WITH_USER.limitedAccess(),
        person: Person = PersonGenerator.RESTRICTION,
        start: ZonedDateTime = ZonedDateTime.now(),
        endDateTime: ZonedDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Restriction(person.limitedAccess(), user, start, endDateTime, id)

    private fun Person.limitedAccess() = LimitedAccessPerson(crn, exclusionMessage, restrictionMessage, id)
    private fun AuditUser.limitedAccess() = LimitedAccessUser(username, id)
    private fun StaffWithUser.limitedAccess() = LimitedAccessUser(user!!.username, user!!.id)
}
