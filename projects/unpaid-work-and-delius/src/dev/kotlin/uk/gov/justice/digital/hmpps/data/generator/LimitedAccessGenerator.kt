package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.controller.casedetails.entity.CaseEntity
import uk.gov.justice.digital.hmpps.entity.Exclusion
import uk.gov.justice.digital.hmpps.entity.LimitedAccessPerson
import uk.gov.justice.digital.hmpps.entity.LimitedAccessUser
import uk.gov.justice.digital.hmpps.entity.Restriction
import uk.gov.justice.digital.hmpps.user.AuditUser
import java.time.ZonedDateTime

object LimitedAccessGenerator {
    val EXCLUSION = generateExclusion(CaseGenerator.EXCLUSION)
    val RESTRICTION = generateRestriction(CaseGenerator.RESTRICTION, endDateTime = ZonedDateTime.now().plusHours(1))

    fun generateExclusion(
        person: CaseEntity,
        user: AuditUser = UserGenerator.LIMITED_ACCESS_USER,
        start: ZonedDateTime = ZonedDateTime.now(),
        endDateTime: ZonedDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Exclusion(person.limitedAccess(), user.limitedAccess(), start, endDateTime, id)

    fun generateRestriction(
        person: CaseEntity,
        user: AuditUser = UserGenerator.AUDIT_USER,
        start: ZonedDateTime = ZonedDateTime.now(),
        endDateTime: ZonedDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Restriction(person.limitedAccess(), user.limitedAccess(), start, endDateTime, id)

    private fun CaseEntity.limitedAccess() = LimitedAccessPerson(crn, exclusionMessage, restrictionMessage, id)
    private fun AuditUser.limitedAccess() = LimitedAccessUser(username, id)
}
