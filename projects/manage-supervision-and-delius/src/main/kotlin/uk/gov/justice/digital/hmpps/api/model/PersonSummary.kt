package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class PersonSummary(
    val name: Name,
    val crn: String,
    val offenderId: Long,
    val pnc: String?,
    val noms: String?,
    val dateOfBirth: LocalDate
)
