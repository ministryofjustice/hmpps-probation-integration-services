package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class AccreditedProgramme(
    val programme: String? = null,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val endReason: String? = null,
    val description: String? = null,
    val active: Boolean = false,
    val notes: String? = null,
)
