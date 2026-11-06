package uk.gov.justice.digital.hmpps.api.model.conviction

import uk.gov.justice.digital.hmpps.api.model.KeyValue
import uk.gov.justice.digital.hmpps.api.model.StaffHuman
import java.time.LocalDateTime

data class CourtReportMinimal(
    val courtReportId: Long,
    val offenderId: Long,
    val requestedDate: LocalDateTime,
    val requiredDate: LocalDateTime,
    val allocationDate: LocalDateTime? = null,
    val completedDate: LocalDateTime? = null,
    val sentToCourtDate: LocalDateTime? = null,
    val receivedByCourtDate: LocalDateTime? = null,
    val courtReportType: KeyValue? = null,
    val reportManagers: List<ReportManager> = emptyList(),
    val deliveredCourtReportType: KeyValue? = null,

    )

data class ReportManager(
    val staff: StaffHuman?,
    val active: Boolean
)