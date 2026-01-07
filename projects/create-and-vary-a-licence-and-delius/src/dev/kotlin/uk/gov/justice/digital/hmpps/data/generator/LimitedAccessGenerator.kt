package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator.DEFAULT_EXCLUDED_STAFF_USER
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator.DEFAULT_RESTRICTED_STAFF_USER
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator.DEFAULT_STAFF_USER
import uk.gov.justice.digital.hmpps.entity.Exclusion
import uk.gov.justice.digital.hmpps.entity.LimitedAccessPerson
import uk.gov.justice.digital.hmpps.entity.LimitedAccessUser
import uk.gov.justice.digital.hmpps.entity.Restriction
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.StaffUser

object LimitedAccessGenerator {
    val LAO_DEFAULT_USER = generateLAOUser(DEFAULT_STAFF_USER)
    val LAO_EXCLUDED_USER = generateLAOUser(DEFAULT_EXCLUDED_STAFF_USER)
    val LAO_RESTRICTED_USER = generateLAOUser(DEFAULT_RESTRICTED_STAFF_USER)

    fun generateLAOUser(user: StaffUser) = LimitedAccessUser(
        username = user.username,
        id = user.id
    )

    val LAO_EXCLUDED_PERSON = LimitedAccessPerson(
        crn = "E123456",
        exclusionMessage = "You do not have access to this person.",
        restrictionMessage = null,
        id = 1L
    )

    val LAO_RESTRICTED_PERSON = LimitedAccessPerson(
        crn = "R123456",
        exclusionMessage = null,
        restrictionMessage = "You do not have access to this person.",
        id = 2L
    )
    val LAO_EXCLUSION = Exclusion(LAO_EXCLUDED_PERSON, LAO_EXCLUDED_USER, null, IdGenerator.getAndIncrement())
    val LAO_RESTRICTION = Restriction(LAO_RESTRICTED_PERSON, LAO_RESTRICTED_USER, null, IdGenerator.getAndIncrement())
}