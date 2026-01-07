package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Exclusion
import uk.gov.justice.digital.hmpps.entity.LimitedAccessPerson
import uk.gov.justice.digital.hmpps.entity.LimitedAccessUser
import uk.gov.justice.digital.hmpps.entity.Restriction

object LimitedAccessGenerator {
    val LAO_DEFAULT_USER = LimitedAccessUser("mr-default", IdGenerator.getAndIncrement())
    val LAO_EXCLUDED_USER = LimitedAccessUser("mr-excluded", IdGenerator.getAndIncrement())
    val LAO_RESTRICTED_USER = LimitedAccessUser("mr-restricted", IdGenerator.getAndIncrement())

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