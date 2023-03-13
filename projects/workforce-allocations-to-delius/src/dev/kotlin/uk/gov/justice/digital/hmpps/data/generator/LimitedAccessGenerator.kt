package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.user.Exclusion
import uk.gov.justice.digital.hmpps.integrations.delius.user.Restriction
import uk.gov.justice.digital.hmpps.user.User
import java.time.LocalDateTime

object LimitedAccessGenerator {
    val EXCLUSION = generateExclusion()
    val RESTRICTION = generateRestriction(endDateTime = LocalDateTime.now().plusHours(1))

    fun generateExclusion(
        user: User = UserGenerator.LIMITED_ACCESS_USER,
        person: Person = PersonGenerator.EXCLUSION,
        endDateTime: LocalDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Exclusion(id, person, user, endDateTime)

    fun generateRestriction(
        user: User = UserGenerator.LIMITED_ACCESS_USER,
        person: Person = PersonGenerator.RESTRICTION,
        endDateTime: LocalDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Restriction(id, person, user, endDateTime)
}
