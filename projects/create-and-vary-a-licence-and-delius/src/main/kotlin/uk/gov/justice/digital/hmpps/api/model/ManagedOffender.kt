package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class ManagedOffender(
    val crn: String,
    val name: Name,
    val allocationDate: LocalDate?,
    val staff: Staff,
    val team: Team?
)
