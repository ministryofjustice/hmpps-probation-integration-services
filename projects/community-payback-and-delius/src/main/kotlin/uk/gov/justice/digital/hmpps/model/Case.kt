package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class Case(
    val crn: String,
    val name: PersonName,
    val dateOfBirth: LocalDate,
    val currentExclusion: Boolean,
    val exclusionMessage: String?,
    val currentRestriction: Boolean,
    val restrictionMessage: String?
)
