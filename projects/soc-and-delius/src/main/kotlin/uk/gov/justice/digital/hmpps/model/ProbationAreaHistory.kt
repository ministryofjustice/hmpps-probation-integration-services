package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class ProbationAreaHistory(
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val probationArea: CodeDescription,
)
