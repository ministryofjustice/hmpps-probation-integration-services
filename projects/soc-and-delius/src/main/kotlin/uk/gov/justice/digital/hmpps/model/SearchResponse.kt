package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class ProbationCase(
    val firstName: String,
    val surname: String,
    val dateOfBirth: LocalDate,
    val crn: String,
    val nomisId: String?,
    val pncNumber: String?,
    val communityManager: Manager,
)

data class ProbationCases(val content: List<ProbationCase>)