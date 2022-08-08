package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeCode

object ContactTypeGenerator {
    val OFFENDER_MANAGER_TRANSFER = generate(ContactTypeCode.OFFENDER_MANAGER_TRANSFER.value)
    val ORDER_SUPERVISOR_TRANSFER = generate(ContactTypeCode.ORDER_SUPERVISOR_TRANSFER.value)
    val RESPONSIBLE_OFFICER_CHANGE = generate(ContactTypeCode.RESPONSIBLE_OFFICER_CHANGE.value)
    val SENTENCE_COMPONENT_TRANSFER = generate(ContactTypeCode.SENTENCE_COMPONENT_TRANSFER.value)

    fun generate(
        code: String,
        id: Long = IdGenerator.getAndIncrement(),
        isSensitive: Boolean = false
    ) = ContactType(
        id,
        code,
        isSensitive
    )
}
