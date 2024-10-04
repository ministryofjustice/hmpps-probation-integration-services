package uk.gov.justice.digital.hmpps.integrations.prison

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS

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

    val suspensionDateIfReset =
        if (conditionalReleaseDate != null && sentenceExpiryDate != null && sentenceExpiryDate > conditionalReleaseDate) {
            conditionalReleaseDate.plusDays(DAYS.between(conditionalReleaseDate, sentenceExpiryDate) * 2 / 3)
        } else null
}
