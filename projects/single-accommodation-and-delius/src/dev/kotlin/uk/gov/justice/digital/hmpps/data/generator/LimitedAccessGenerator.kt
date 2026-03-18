package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Exclusion
import uk.gov.justice.digital.hmpps.entity.LimitedAccessPerson
import uk.gov.justice.digital.hmpps.entity.LimitedAccessUser
import uk.gov.justice.digital.hmpps.entity.Restriction
import java.time.LocalDateTime

object LimitedAccessGenerator {
    val EXCLUDED_USER = generateLaoUser(UserGenerator.DEFAULT.username, UserGenerator.DEFAULT.id)
    val RESTRICTED_USER = generateLaoUser("OtherUser")
    val EXCLUDED_CASE = generateLaoPerson(
        id = IdGenerator.getAndIncrement(), crn = "E123456",
        exclusionMessage = "This case is excluded.",
        restrictionMessage = null
    )
    val RESTRICTED_CASE = generateLaoPerson(
        id = IdGenerator.getAndIncrement(), crn = "R123456",
        exclusionMessage = null,
        restrictionMessage = "This case is restricted."
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
        user: LimitedAccessUser = EXCLUDED_USER,
        endDateTime: LocalDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Exclusion(person, user, endDateTime, id)

    fun generateRestriction(
        person: LimitedAccessPerson,
        user: LimitedAccessUser = RESTRICTED_USER,
        endDateTime: LocalDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Restriction(person, user, endDateTime, id)
}
