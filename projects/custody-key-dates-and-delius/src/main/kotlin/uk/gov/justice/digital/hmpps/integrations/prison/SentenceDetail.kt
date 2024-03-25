package uk.gov.justice.digital.hmpps.integrations.prison

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.LocalDate

class SentenceDetail(
    val sentenceExpiryDate: LocalDate? = null,
    val confirmedReleaseDate: LocalDate? = null,
    conditionalReleaseDate: LocalDate? = null,
    conditionalReleaseOverrideDate: LocalDate? = null,
    val licenceExpiryDate: LocalDate? = null,
    val paroleEligibilityDate: LocalDate? = null,
    @JsonAlias("topupSupervisionExpiryDate")
    val postSentenceSupervisionEndDate: LocalDate? = null,
    val homeDetentionCurfewEligibilityDate: LocalDate? = null
) {
    val conditionalReleaseDate = conditionalReleaseOverrideDate ?: conditionalReleaseDate
}
