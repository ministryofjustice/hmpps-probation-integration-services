package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class GetAppointmentsRequest(
    val requirementIds: List<Long>,
    val licenceConditionIds: List<Long>,
    val fromDate: LocalDate,
    val toDate: LocalDate,
)