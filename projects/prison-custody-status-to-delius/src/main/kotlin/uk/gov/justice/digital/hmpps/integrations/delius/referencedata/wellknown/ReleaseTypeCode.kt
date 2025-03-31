package uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown

enum class ReleaseTypeCode(val code: String) {
    ADULT_LICENCE("ADL"),
    HDC_ADULT_LICENCE("HDC"),
    END_CUSTODY_SUPERVISED_LICENCE("ECSL")
}
