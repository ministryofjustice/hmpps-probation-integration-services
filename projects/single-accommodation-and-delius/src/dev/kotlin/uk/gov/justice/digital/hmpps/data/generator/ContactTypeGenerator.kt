package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.contact.ContactType

object ContactTypeGenerator {
    val DUTY_TO_REFER_CREATED = ContactType(id(), ContactType.DUTY_TO_REFER_CREATED)
    val DUTY_TO_REFER_UPDATED = ContactType(id(), ContactType.DUTY_TO_REFER_UPDATED)
}
