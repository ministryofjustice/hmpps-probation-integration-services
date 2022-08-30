package uk.gov.justice.digital.hmpps.integrations.delius.contact.type

enum class ContactTypeCode(val code: String) {
    RELEASE_FROM_CUSTODY("EREL"),
    COMPONENT_TERMINATED("ETER"),
    BREACH_PRISON_RECALL("ERCL"),
    PRISON_MANAGER_AUTOMATIC_TRANSFER("EPOMAT"),
    RESPONSIBLE_OFFICER_CHANGE("ROC"),
    COMPONENT_PROVIDER_TRANSFER_REJECTED("ETCX"),
}
