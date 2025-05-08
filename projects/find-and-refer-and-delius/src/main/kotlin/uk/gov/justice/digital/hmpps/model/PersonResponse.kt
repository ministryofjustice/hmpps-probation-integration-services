package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class PersonResponse(
    val crn: String,
    val nomsNumber: String? = null,
    val name: Name,
    val dateOfBirth: LocalDate,
    val ethnicity: String? = null,
    val gender: String? = null,
)
