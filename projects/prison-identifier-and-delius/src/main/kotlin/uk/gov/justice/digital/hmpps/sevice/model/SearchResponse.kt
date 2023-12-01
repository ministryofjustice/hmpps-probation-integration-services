package uk.gov.justice.digital.hmpps.sevice.model

import java.time.LocalDate

data class SearchResponse(
    val content: List<PrisonSearchResult>
)

data class SearchRequest(
    val prisonerIdentifier: String?,
    val firstName: String,
    val lastName: String,
    val gender: String?,
    val dateOfBirth: LocalDate,
    val includeAliases: Boolean = true
)

data class PrisonSearchResult(
    val prisonerNumber: String,
    val pncNumber: String?,
    val croNumber: String?,
    val sentenceStartDate: LocalDate?
)
