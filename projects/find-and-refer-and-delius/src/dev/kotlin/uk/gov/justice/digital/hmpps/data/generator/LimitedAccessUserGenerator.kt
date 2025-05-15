package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.LimitedAccessUser

object LimitedAccessUserGenerator {
    val EXCLUSION_USER = generateLimitedAccessUser("jim-brown")
    val RESTRICTION_USER = generateLimitedAccessUser("philip-smith")
    val RESTRICTION_AND_EXCLUSION_USER = generateLimitedAccessUser("ian-smith")

    fun generateLimitedAccessUser(username: String) =
        LimitedAccessUser(id = IdGenerator.getAndIncrement(), username = username)
}
