package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.common.entity.contact.type.ContactType
import uk.gov.justice.digital.hmpps.integrations.common.entity.contact.type.ContactTypeCode

object ContactTypeGenerator {
    val DEFAULT = generate()

    fun generate(id: Long = IdGenerator.getAndIncrement()) = ContactType(id, ContactTypeCode.UPW_ASSESSMENT.code)
}
