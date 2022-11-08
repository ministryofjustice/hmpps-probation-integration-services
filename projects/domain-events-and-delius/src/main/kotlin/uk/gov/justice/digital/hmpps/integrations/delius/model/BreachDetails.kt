package uk.gov.justice.digital.hmpps.integrations.delius.model

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class BreachDetails(
    val crn: String,
    val eventNumber: String? = null,
)
