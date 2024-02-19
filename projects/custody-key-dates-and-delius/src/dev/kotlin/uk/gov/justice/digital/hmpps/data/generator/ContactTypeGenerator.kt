package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.contact.ContactType

object ContactTypeGenerator {
    val EDSS = ContactType(IdGenerator.getAndIncrement(), "EDSS")
}
