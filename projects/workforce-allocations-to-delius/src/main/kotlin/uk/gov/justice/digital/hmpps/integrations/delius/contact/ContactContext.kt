package uk.gov.justice.digital.hmpps.integrations.delius.contact

data class ContactContext(
    val contactType: ContactType,
    val offenderId: Long,
    val eventId: Long? = null,
    val requirementId: Long? = null,
)

enum class ContactTypeCode(val value: String) {
    OFFENDER_MANAGER_TRANSFER("ETOM"),
    ORDER_SUPERVISOR_TRANSFER("ETOS"),
    RESPONSIBLE_OFFICER_CHANGE("ROC"),
    SENTENCE_COMPONENT_TRANSFER("ETRC"),
    INITIAL_APPOINTMENT_IN_OFFICE("COAI"),
    INITIAL_APPOINTMENT_ON_DOORSTEP("CODI"),
    INITIAL_APPOINTMENT_HOME_VISIT("COHV"),
    INITIAL_APPOINTMENT_BY_VIDEO("COVI"),
    CASE_ALLOCATION_DECISION_EVIDENCE("CADE"),
}
