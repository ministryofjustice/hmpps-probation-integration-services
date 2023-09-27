package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Exclusion
import uk.gov.justice.digital.hmpps.entity.LimitedAccessPerson
import uk.gov.justice.digital.hmpps.entity.LimitedAccessUser
import uk.gov.justice.digital.hmpps.entity.Restriction
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.user.AuditUser
import java.time.LocalDateTime

object LimitedAccessGenerator {
    val EXCLUSION = generateExclusion(PersonGenerator.EXCLUSION)
    val RESTRICTION = generateRestriction(PersonGenerator.RESTRICTION, endDateTime = LocalDateTime.now().plusHours(1))

    fun generateExclusion(
        person: Person,
        user: AuditUser = UserGenerator.LIMITED_ACCESS_USER,
        endDateTime: LocalDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Exclusion(person.limitedAccess(), user.limitedAccess(), endDateTime, id)

    fun generateRestriction(
        person: Person,
        user: AuditUser = UserGenerator.AUDIT_USER,
        endDateTime: LocalDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Restriction(person.limitedAccess(), user.limitedAccess(), endDateTime, id)

    private fun Person.limitedAccess() = LimitedAccessPerson(crn, exclusionMessage, restrictionMessage, id)
    private fun AuditUser.limitedAccess() = LimitedAccessUser(username, id)
}
