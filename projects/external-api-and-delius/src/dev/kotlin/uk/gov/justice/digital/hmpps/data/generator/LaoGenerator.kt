package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Exclusion
import uk.gov.justice.digital.hmpps.entity.LimitedAccessPerson
import uk.gov.justice.digital.hmpps.entity.LimitedAccessUser
import uk.gov.justice.digital.hmpps.entity.Restriction
import uk.gov.justice.digital.hmpps.integration.delius.entity.Person
import uk.gov.justice.digital.hmpps.user.AuditUser
import java.time.ZonedDateTime

object LaoGenerator {
    val EXCLUSION = generateExclusion(PersonGenerator.EXCLUSION)
    val RESTRICTION = generateRestriction(PersonGenerator.RESTRICTION, endDateTime = ZonedDateTime.now().plusDays(1))
    val BOTH_RESTRICTION = generateRestriction(PersonGenerator.RESTRICTION_EXCLUSION)
    val BOTH_EXCLUSION = generateExclusion(PersonGenerator.RESTRICTION_EXCLUSION)

    fun generateExclusion(
        person: Person,
        user: AuditUser = UserGenerator.LIMITED_ACCESS_USER,
        start: ZonedDateTime = ZonedDateTime.now(),
        endDateTime: ZonedDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Exclusion(person.limitedAccess(), user.limitedAccess(), start, endDateTime, id)

    fun generateRestriction(
        person: Person,
        user: AuditUser = UserGenerator.AUDIT_USER,
        start: ZonedDateTime = ZonedDateTime.now(),
        endDateTime: ZonedDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Restriction(person.limitedAccess(), user.limitedAccess(), start, endDateTime, id)

    private fun Person.limitedAccess() = LimitedAccessPerson(crn, exclusionMessage, restrictionMessage, id)
    private fun AuditUser.limitedAccess() = LimitedAccessUser(username, id)
}