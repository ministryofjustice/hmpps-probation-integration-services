package uk.gov.justice.digital.hmpps.client

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.PostExchange
import java.time.LocalDate

interface ProbationSearchClient {
    @PostExchange(url = "/search/activity")
    fun contactSearch(
        @RequestBody body: ActivitySearchRequest,
        @RequestParam page: Int = 0,
        @RequestParam size: Int = 10
    ): ContactSearchResponse
}

data class ActivitySearchRequest(
    val crn: String,
    val keywords: String? = "",
    @JsonFormat(pattern = "yyyy-MM-dd")
    val dateFrom: LocalDate? = null,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val dateTo: LocalDate? = null,
    val filters: List<String> = emptyList(),
)

data class ContactSearchResponse(
    val size: Int,
    val page: Int,
    val totalResults: Long,
    val totalPages: Int,
    val results: List<ContactSearchResult>,
)

data class ContactSearchResult(
    val crn: String,
    val id: Long,
    val highlights: Map<String, List<String>> = mapOf()
)
