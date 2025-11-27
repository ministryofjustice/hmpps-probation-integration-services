package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class PersonalDetails(
    val crn: String,
    val name: Name,
    val dateOfBirth: LocalDate
)