package uk.gov.justice.digital.hmpps.api.model.activity

import com.fasterxml.jackson.annotation.JsonFormat
import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import java.time.LocalDate

data class PersonActivity(
    val personSummary: PersonSummary,
    val activities: List<Activity>
)

data class PersonActivitySearchResponse(
    val size: Int,
    val page: Int,
    val totalResults: Long,
    val totalPages: Int,
    val personSummary: PersonSummary,
    val activities: List<Activity>
)

data class PersonActivitySearchRequest(
    val keywords: String? = "",
    @JsonFormat(pattern = "yyyy-MM-dd")
    val dateFrom: LocalDate? = null,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val dateTo: LocalDate? = null,
    val filters: List<String> = emptyList(),
)
