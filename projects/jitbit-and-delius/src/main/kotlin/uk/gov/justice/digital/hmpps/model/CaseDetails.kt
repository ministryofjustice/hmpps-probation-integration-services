package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class CaseDetails(
    val name: Name,
    val dateOfBirth: LocalDate,
    val mainAddress: Address?,
)
