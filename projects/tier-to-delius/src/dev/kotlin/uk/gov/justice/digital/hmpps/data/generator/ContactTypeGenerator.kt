package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode

object ContactTypeGenerator {
    val TIER_UPDATE = generate(ContactTypeCode.TIER_UPDATE.code)
    fun generate(
        code: String,
        id: Long = IdGenerator.getAndIncrement()
    ) = ContactType(id, code)
}
