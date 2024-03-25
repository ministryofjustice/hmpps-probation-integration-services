package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactType

object ContactTypeGenerator {
    val EARS_CONTACT_TYPE = ContactType(
        IdGenerator.getAndIncrement(),
        "EARS",
        false
    )
    val EACA_CONTACT_TYPE = ContactType(
        IdGenerator.getAndIncrement(),
        "EACA",
        false
    )
    val EACO_CONTACT_TYPE = ContactType(
        IdGenerator.getAndIncrement(),
        "EACO",
        false
    )
    val EABP_CONTACT_TYPE = ContactType(
        IdGenerator.getAndIncrement(),
        "EABP",
        false
    )
    val EADP_CONTACT_TYPE = ContactType(
        IdGenerator.getAndIncrement(),
        "EADP",
        false
    )
    val EAAR_CONTACT_TYPE = ContactType(
        IdGenerator.getAndIncrement(),
        "EAAR",
        false
    )
}
