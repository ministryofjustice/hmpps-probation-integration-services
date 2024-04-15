package uk.gov.justice.digital.hmpps.api.model.activity

import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.personalDetails.Document
import uk.gov.justice.digital.hmpps.api.model.schedule.OfficeAddress
import java.time.ZonedDateTime

data class Activity(

    val id: Long,
    val eventNumber: String?,
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
    val rescheduledStaff: Boolean,
    val rescheduledPop: Boolean,
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
    val isAppointment: Boolean = false,
    val isCommunication: Boolean = false,
    val action: String?,
    val isSystemContact: Boolean? = false,
    val isEmailOrTextFromPop: Boolean? = false,
    val isPhoneCallFromPop: Boolean? = false,
    val isEmailOrTextToPop: Boolean? = false,
    val isPhoneCallToPop: Boolean? = false,
    val isPastAppointment: Boolean = (isAppointment && ZonedDateTime.now() > startDateTime),
    val countsTowardsRAR: Boolean?,
    val lastUpdated: ZonedDateTime,
    val lastUpdatedBy: Name
)
