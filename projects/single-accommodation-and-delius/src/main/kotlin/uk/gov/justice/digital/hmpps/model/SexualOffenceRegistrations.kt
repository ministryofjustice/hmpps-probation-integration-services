package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class SexualOffenceRegistrations(
    val crn: String,
    val sexualOffenceRegistrations: List<SexualOffenceRegistration>?
)

data class SexualOffenceRegistration(
    val type: CodeDescription,
    val category: CodeDescription? = null,
    val startDate: LocalDate,
    val nextReviewDate: LocalDate?,
    val endDate: LocalDate?,
)
