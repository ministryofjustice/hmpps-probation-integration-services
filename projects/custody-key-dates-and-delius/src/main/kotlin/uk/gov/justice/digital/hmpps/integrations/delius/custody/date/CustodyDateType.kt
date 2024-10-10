package uk.gov.justice.digital.hmpps.integrations.delius.custody.date

enum class CustodyDateType(val code: String) {
    LICENCE_EXPIRY_DATE("LED"),
    AUTOMATIC_CONDITIONAL_RELEASE_DATE("ACR"),
    PAROLE_ELIGIBILITY_DATE("PED"),
    SENTENCE_EXPIRY_DATE("SED"),
    EXPECTED_RELEASE_DATE("EXP"),
    HDC_EXPECTED_DATE("HDE"),
    POST_SENTENCE_SUPERVISION_END_DATE("PSSED"),
    SUSPENSION_DATE_IF_RESET("PR1")
}
