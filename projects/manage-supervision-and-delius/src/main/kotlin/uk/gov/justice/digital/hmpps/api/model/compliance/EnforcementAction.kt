package uk.gov.justice.digital.hmpps.api.model.compliance

import java.time.LocalDate

data class EnforcementAction(
    val code: String?,
    val description: String?,
    val responseByDate: LocalDate?,
)
