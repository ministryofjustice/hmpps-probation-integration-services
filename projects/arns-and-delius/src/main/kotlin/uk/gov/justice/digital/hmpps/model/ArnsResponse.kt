package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class ArnsResponse(
    val crn: String,
    val dateOfBirth: LocalDate,
    val gender: String,
    val offences: List<OffenceDetail> = emptyList(),
)

data class OffenceDetail(
    val mainOffence: String?,
    val sentenceStartDate: LocalDate?,
    val convictionDate: LocalDate?,
)
