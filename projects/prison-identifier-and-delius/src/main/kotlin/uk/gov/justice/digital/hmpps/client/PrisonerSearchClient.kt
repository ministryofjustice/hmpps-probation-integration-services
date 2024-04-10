package uk.gov.justice.digital.hmpps.client

import com.fasterxml.jackson.annotation.JsonAlias
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.PostExchange
import java.time.LocalDate

interface PrisonerSearchClient {
    @PostExchange(url = "/global-search")
    fun globalSearch(@RequestBody body: PrisonerSearchRequest): PrisonerSearchResponse
}

data class PrisonerSearchRequest(
    val prisonerIdentifier: String?,
    val firstName: String,
    val lastName: String,
    val gender: String?,
    val dateOfBirth: LocalDate,
    val includeAliases: Boolean = true
)

data class PrisonerSearchResponse(
    val content: List<PrisonerSearchResult>
)

data class PrisonerSearchResult(
    val firstName: String,
    val lastName: String,
    val prisonerNumber: String,
    @JsonAlias("bookNumber")
    val bookingNumber: String?,
    @JsonAlias("pncNumberCanonicalLong")
    val pncNumber: String?,
    val croNumber: String?,
    val sentenceStartDate: LocalDate?,
    val dateOfBirth: LocalDate
)