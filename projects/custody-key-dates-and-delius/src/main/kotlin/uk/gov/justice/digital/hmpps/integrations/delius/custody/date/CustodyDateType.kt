package uk.gov.justice.digital.hmpps.integrations.delius.custody.date

import uk.gov.justice.digital.hmpps.integrations.prison.SentenceDetail
import java.time.LocalDate
import kotlin.reflect.KProperty

enum class CustodyDateType(val code: String, val field: KProperty<LocalDate?>) {
    LICENCE_EXPIRY_DATE("LED", SentenceDetail::licenceExpiryDate),
    AUTOMATIC_CONDITIONAL_RELEASE_DATE("ACR", SentenceDetail::conditionalReleaseDate),
    PAROLE_ELIGIBILITY_DATE("PED", SentenceDetail::paroleEligibilityDate),
    SENTENCE_EXPIRY_DATE("SED", SentenceDetail::sentenceExpiryDate),
    EXPECTED_RELEASE_DATE("EXP", SentenceDetail::confirmedReleaseDate),
    HDC_EXPECTED_DATE("HDE", SentenceDetail::homeDetentionCurfewEligibilityDate),
    POST_SENTENCE_SUPERVISION_END_DATE("PSSED", SentenceDetail::postSentenceSupervisionEndDate),
}
