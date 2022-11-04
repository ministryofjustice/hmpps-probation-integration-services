package uk.gov.justice.digital.hmpps.integrations.delius.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDate

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class BreachDetails(
    val referralDate: LocalDate,
    val outcome: Outcome? = null,
    val status: Status,
    val eventNumber: String? = null,
)

data class Outcome(
    val code: String,
    val description: String,
)

data class Status(
    val code: String,
    val description: String,
)
