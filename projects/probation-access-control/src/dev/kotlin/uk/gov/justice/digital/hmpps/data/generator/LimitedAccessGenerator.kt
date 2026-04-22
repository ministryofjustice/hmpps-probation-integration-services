package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Exclusion
import uk.gov.justice.digital.hmpps.entity.LimitedAccessPerson
import uk.gov.justice.digital.hmpps.entity.LimitedAccessUser
import uk.gov.justice.digital.hmpps.entity.Restriction
import java.time.LocalDateTime

object LimitedAccessGenerator {

    val EXCLUSION = generateExclusion(
        person = PersonGenerator.EXCLUDED,
        user = UserGenerator.DEFAULT
    )

    val RESTRICTION = generateRestriction(
        person = PersonGenerator.RESTRICTED,
        user = UserGenerator.RESTRICTED
    )

    fun generateExclusion(
        person: LimitedAccessPerson,
        user: LimitedAccessUser,
        endDateTime: LocalDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Exclusion(person, user, endDateTime, id)

    fun generateRestriction(
        person: LimitedAccessPerson,
        user: LimitedAccessUser,
        endDateTime: LocalDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Restriction(person, user, endDateTime, id)
}