package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Exclusion
import uk.gov.justice.digital.hmpps.entity.LimitedAccessPerson
import uk.gov.justice.digital.hmpps.entity.LimitedAccessUser
import uk.gov.justice.digital.hmpps.entity.Restriction
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

object LimitedAccessGenerator {

    val EXCLUSION = generateExclusion(
        person = PersonGenerator.EXCLUDED,
        user = UserGenerator.DEFAULT
    )

    val RESTRICTION = generateRestriction(
        person = PersonGenerator.RESTRICTED,
        user = UserGenerator.RESTRICTED
    )

    val BOTH_EXCLUSION = generateExclusion(
        person = PersonGenerator.BOTH,
        user = UserGenerator.DEFAULT
    )

    val BOTH_RESTRICTION = generateRestriction(
        person = PersonGenerator.BOTH,
        user = UserGenerator.RESTRICTED
    )

    fun generateExclusion(
        person: LimitedAccessPerson,
        user: LimitedAccessUser,
        start: LocalDate = LocalDate.now().minusDays(1),
        endDateTime: LocalDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Exclusion(person, user, start, endDateTime, id)

    fun generateRestriction(
        person: LimitedAccessPerson,
        user: LimitedAccessUser,
        start: ZonedDateTime = ZonedDateTime.now().minusDays(1),
        endDateTime: LocalDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Restriction(person, user, start, endDateTime, id)
}