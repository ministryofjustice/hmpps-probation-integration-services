package uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown

enum class InstitutionCode(val code: String) {
    IN_COMMUNITY("COMMUN"),
    UNLAWFULLY_AT_LARGE("UATLRG"),
    UNKNOWN("UNKNOW"),
    OTHER_SECURE_UNIT("XXX056"),
    OTHER_IRC("XXX054")
}
