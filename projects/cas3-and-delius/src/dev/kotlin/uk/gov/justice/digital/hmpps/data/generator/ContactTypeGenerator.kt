package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactType

object ContactTypeGenerator {
    val CONTACT_TYPE = ContactType(
        IdGenerator.getAndIncrement(),
        "EARS",
        false
    )
}
