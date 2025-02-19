package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.Exclusion
import uk.gov.justice.digital.hmpps.integrations.delius.Person
import uk.gov.justice.digital.hmpps.integrations.delius.Restriction
import uk.gov.justice.digital.hmpps.user.AuditUser
import java.time.LocalDateTime

object LimitedAccessGenerator {
    val EXCLUSION = generateExclusion(PersonGenerator.EXCLUSION)
    val RESTRICTION =
        generateRestriction(PersonGenerator.RESTRICTION, endDateTime = LocalDateTime.now().plusDays(1))
    val BOTH_EXCLUSION = generateExclusion(PersonGenerator.RESTRICTION_EXCLUSION)
    val BOTH_RESTRICTION = generateRestriction(
        PersonGenerator.RESTRICTION_EXCLUSION,
        endDateTime = LocalDateTime.now().plusDays(1)
    )

    fun generateExclusion(
        person: Person,
        user: AuditUser = UserGenerator.LIMITED_ACCESS_USER,
        endDateTime: LocalDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Exclusion(person, user, endDateTime, id)

    fun generateRestriction(
        person: Person,
        user: AuditUser = UserGenerator.NON_LAO_USER,
        endDateTime: LocalDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Restriction(person, user, endDateTime, id)
}
