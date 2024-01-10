package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class ManagedOffender(
    val offenderCrn: String,
    val offenderName: Name,
    val allocationDate: LocalDate?,
    val staff: Staff,
    val team: Team?
)
