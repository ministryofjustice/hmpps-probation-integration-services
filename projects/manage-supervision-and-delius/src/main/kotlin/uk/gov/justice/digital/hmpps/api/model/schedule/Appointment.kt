package uk.gov.justice.digital.hmpps.api.model.schedule

import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.personalDetails.Document
import java.time.ZonedDateTime

data class Appointment(

    val id: Long,
    val type: String,
    val startDateTime: ZonedDateTime,
    val endDateTime: ZonedDateTime?,
    val rarToolKit: String?,
    val notes: String?,
    val isSensitive: Boolean?,
    val hasOutcome: Boolean,
    val wasAbsent: Boolean?,
    val officerName: Name?,
    val isInitial: Boolean,
    val isNationalStandard: Boolean,
    var location: OfficeAddress? = null,
    val rescheduled: Boolean,
    val didTheyComply: Boolean?,
    val absentWaitingEvidence: Boolean?,
    val rearrangeOrCancelReason: String?,
    val rescheduledBy: Name?,
    val repeating: Boolean? = null,
    val nonComplianceReason: String?,
    val documents: List<Document>,
    val rarCategory: String?,
    val acceptableAbsence: Boolean?,
    val acceptableAbsenceReason: String?,
    val lastUpdated: ZonedDateTime,
    val lastUpdatedBy: Name
)
