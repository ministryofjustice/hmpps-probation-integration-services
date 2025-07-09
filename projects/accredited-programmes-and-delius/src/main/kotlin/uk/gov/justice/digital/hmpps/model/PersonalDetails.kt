package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate
import java.time.Period

data class PersonalDetails(
    val crn: String,
    val name: Name,
    val dateOfBirth: LocalDate,
    val age: String = with(Period.between(dateOfBirth, LocalDate.now())) { "$years years, $months months" },
    val sex: CodedValue,
    val ethnicity: CodedValue?,
    val probationPractitioner: ProbationPractitioner?,
    val probationDeliveryUnit: CodedValue?,
)
