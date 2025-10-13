package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class ReallocationDetails(
    val crn: String,
    val name: Name,
    val dateOfBirth: LocalDate,
    val manager: Manager,
    val hasActiveOrder: Boolean
)