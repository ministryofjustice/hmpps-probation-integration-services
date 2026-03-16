package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class CaseListResponse(
    val cases: List<Case>
)

data class Case(
    val crn: String,
    val name: Name,
    val nomsNumber: String?,
    val pncNumber: String?,
    val dateOfBirth: LocalDate,
    val staff: Officer,
    val team: CodeDescription,
    val gender: String,
    val roshLevel: String?,
    val tier: String?,
    val expectedReleaseDate: LocalDate?
)

data class Name(
    val forename: String,
    val middleName: String?,
    val surname: String
)

data class Officer(
    val name: Name,
    val username: String,
    val code: String
)

data class CodeDescription(
    val code: String,
    val description: String
)