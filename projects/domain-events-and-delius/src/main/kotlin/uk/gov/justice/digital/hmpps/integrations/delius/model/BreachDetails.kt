package uk.gov.justice.digital.hmpps.integrations.delius.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.ZonedDateTime

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class BreachDetails(
    val referralDate: ZonedDateTime,
    val crn: String,
    val eventNumber: String? = null,
)
