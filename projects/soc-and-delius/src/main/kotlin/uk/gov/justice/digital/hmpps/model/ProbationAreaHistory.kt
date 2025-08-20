package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class ProbationAreaHistory(
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val localAdminUnit: HistoryLau,
)

data class HistoryLau(val code: String, val description: String, val probationDeliveryUnit: HistoryPdu)
data class HistoryPdu(val code: String, val description: String, val probationArea: CodeDescription)
