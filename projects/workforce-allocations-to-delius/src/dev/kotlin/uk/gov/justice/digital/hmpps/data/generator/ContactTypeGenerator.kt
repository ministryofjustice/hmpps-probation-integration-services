package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeCode

object ContactTypeGenerator {
    val OFFENDER_MANAGER_TRANSFER = generate(ContactTypeCode.OFFENDER_MANAGER_TRANSFER.value)
    val ORDER_SUPERVISOR_TRANSFER = generate(ContactTypeCode.ORDER_SUPERVISOR_TRANSFER.value)
    val RESPONSIBLE_OFFICER_CHANGE = generate(ContactTypeCode.RESPONSIBLE_OFFICER_CHANGE.value)
    val SENTENCE_COMPONENT_TRANSFER = generate(ContactTypeCode.SENTENCE_COMPONENT_TRANSFER.value)
    val INITIAL_APPOINTMENT_IN_OFFICE = generate(ContactTypeCode.INITIAL_APPOINTMENT_IN_OFFICE.value)
    val INITIAL_APPOINTMENT_ON_DOORSTEP = generate(ContactTypeCode.INITIAL_APPOINTMENT_ON_DOORSTEP.value)
    val INITIAL_APPOINTMENT_HOME_VISIT = generate(ContactTypeCode.INITIAL_APPOINTMENT_HOME_VISIT.value)
    val INITIAL_APPOINTMENT_BY_VIDEO = generate(ContactTypeCode.INITIAL_APPOINTMENT_BY_VIDEO.value)
    val CASE_ALLOCATION_DECISION_EVIDENCE = generate(ContactTypeCode.CASE_ALLOCATION_DECISION_EVIDENCE.value)
    val CASE_ALLOCATION_SPO_OVERSIGHT = generate(ContactTypeCode.CASE_ALLOCATION_SPO_OVERSIGHT.value)

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
