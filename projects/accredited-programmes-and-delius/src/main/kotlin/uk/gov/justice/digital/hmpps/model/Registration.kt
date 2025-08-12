package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class Registration(
    val type: CodedValue,
    val category: CodedValue?,
    val date: LocalDate,
    val nextReviewDate: LocalDate?,
)