package uk.gov.justice.digital.hmpps.api.model.activity

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
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
    @Schema(
        description = "Whether to include system generated contacts in the search results. Defaults to true.",
        example = "false",
    ) val includeSystemGenerated: Boolean = true,
    val filters: List<String> = emptyList(),
)
