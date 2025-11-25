package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Exclusion
import uk.gov.justice.digital.hmpps.entity.LimitedAccessPerson
import uk.gov.justice.digital.hmpps.entity.LimitedAccessUser
import uk.gov.justice.digital.hmpps.entity.Restriction
import java.time.LocalDateTime

object LimitedAccessGenerator {
    val FULL_ACCESS_USER = generateLaoUser("FullAccess")
    val LIMITED_ACCESS_USER = generateLaoUser("LimitedAccess")
    val EXCLUDED_CASE = generateLaoPerson(
        id = IdGenerator.getAndIncrement(), crn = "E123456",
        exclusionMessage = "This case has an exclusion",
        restrictionMessage = null
    )
    val RESTRICTED_CASE = generateLaoPerson(
        id = IdGenerator.getAndIncrement(), crn = "R123456",
        exclusionMessage = null,
        restrictionMessage = "This case has an restriction",
    )

    private fun generateLaoPerson(
        id: Long,
        crn: String,
        exclusionMessage: String? = null,
        restrictionMessage: String? = null,
    ) = LimitedAccessPerson(crn, exclusionMessage, restrictionMessage, id)

    fun generateLaoUser(username: String, id: Long = IdGenerator.getAndIncrement()) = LimitedAccessUser(username, id)

    fun generateExclusion(
        person: LimitedAccessPerson,
        user: LimitedAccessUser = LIMITED_ACCESS_USER,
        endDateTime: LocalDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Exclusion(person, user, endDateTime, id)

    fun generateRestriction(
        person: LimitedAccessPerson,
        user: LimitedAccessUser = FULL_ACCESS_USER,
        endDateTime: LocalDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Restriction(person, user, endDateTime, id)
}