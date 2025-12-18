package uk.gov.justice.digital.hmpps.controller.model

import java.time.LocalDate

data class PersonDetails(
    val crn: String,
    val name: Name,
    val dateOfBirth: LocalDate,
    val age: Long
)

data class Name(
    val forenames: String,
    val surname: String
)
