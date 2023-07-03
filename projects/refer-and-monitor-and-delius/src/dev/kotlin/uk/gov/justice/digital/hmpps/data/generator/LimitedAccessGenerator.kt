package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.limitedaccess.entity.Exclusion
import uk.gov.justice.digital.hmpps.integrations.delius.limitedaccess.entity.Restriction
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
    ) = Exclusion(person, user, endDateTime, id)

    fun generateRestriction(
        person: Person,
        user: AuditUser = UserGenerator.AUDIT_USER,
        endDateTime: LocalDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Restriction(person, user, endDateTime, id)
}
