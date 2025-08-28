package uk.gov.justice.digital.hmpps.model

import jakarta.validation.constraints.Size
import java.time.LocalDate

data class GetAppointmentsRequest(
    @Size(max = 500)
    val requirementIds: List<Long>,
    @Size(max = 500)
    val licenceConditionIds: List<Long>,
    val fromDate: LocalDate,
    val toDate: LocalDate,
)