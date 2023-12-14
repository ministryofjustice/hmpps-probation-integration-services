package uk.gov.justice.digital.hmpps.sevice.model

import com.fasterxml.jackson.annotation.JsonAlias
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
    val firstName: String,
    val lastName: String,
    val prisonerNumber: String,
    @JsonAlias("bookNumber")
    val bookingNumber: String?,
    val pncNumber: String?,
    val croNumber: String?,
    val sentenceStartDate: LocalDate?,
    val dateOfBirth: LocalDate
)
