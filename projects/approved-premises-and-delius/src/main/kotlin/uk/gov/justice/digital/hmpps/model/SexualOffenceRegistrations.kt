package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class SexualOffenceRegistrations(
    val crn: String,
    val sexualOffenceRegistrations: List<SexualOffenceRegistration>?
)

data class SexualOffenceRegistration(
    val type: CodeDescription,
    val category: CodeDescription? = null,
    val date: LocalDate,
    val nextReviewDate: LocalDate?,
    val endDate: LocalDate?,
)

data class CodeDescription(
    val code: String?,
    val description: String?
)