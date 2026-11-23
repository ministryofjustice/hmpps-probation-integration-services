package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class AdditionalSentences(
    val additionalSentences: List<AdditionalSentenceDates>?
)

data class AdditionalSentenceDates(
    val eventId: Long,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
)
