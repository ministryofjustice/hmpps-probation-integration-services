package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class ActiveCasesResponse(
    val code: String,
    val name: Name,
    val grade: String?,
    val email: String?,
    val cases: List<Case>
)

data class Case(
    val crn: String,
    val name: Name,
    val type: String,
    val initialAllocationDate: LocalDate?
)
