package uk.gov.justice.digital.hmpps.api.model.activity

import uk.gov.justice.digital.hmpps.api.model.Manager
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.personalDetails.Document
import uk.gov.justice.digital.hmpps.api.model.schedule.OfficeAddress
import uk.gov.justice.digital.hmpps.api.model.sentence.NoteDetail
import java.time.ZonedDateTime

data class Activity(
    val id: Long,
    val eventNumber: String? = null,
    val type: String,
    val startDateTime: ZonedDateTime,
    val endDateTime: ZonedDateTime?,
    val rarToolKit: String? = null,
    val appointmentNotes: List<NoteDetail>? = null,
    val appointmentNote: NoteDetail? = null,
    val isSensitive: Boolean?,
    val hasOutcome: Boolean?,
    val wasAbsent: Boolean?,
    val officer: Manager? = null,
    val isInitial: Boolean,
    val isNationalStandard: Boolean,
    var location: OfficeAddress? = null,
    val rescheduled: Boolean,
    val rescheduledStaff: Boolean,
    val rescheduledPop: Boolean,
    val didTheyComply: Boolean?,
    val absentWaitingEvidence: Boolean?,
    val rearrangeOrCancelReason: String?,
    val rescheduledBy: Name? = null,
    val repeating: Boolean? = null,
    val nonComplianceReason: String?,
    val documents: List<Document> = emptyList(),
    val isRarRelated: Boolean? = false,
    val rarCategory: String? = null,
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
    val isInPast: Boolean = (ZonedDateTime.now() > startDateTime),
    val isPastAppointment: Boolean = (isAppointment && isInPast),
    val countsTowardsRAR: Boolean?,
    val lastUpdated: ZonedDateTime,
    val lastUpdatedBy: Name,
    val description: String? = null,
    val outcome: String? = null,
    val deliusManaged: Boolean,
    val isVisor: Boolean? = null,
    val eventId: Long? = null,
    val component: Component? = null,
    val nsiId: Long? = null,
)

data class Component(val id: Long, val description: String, val type: Type) {
    enum class Type {
        LICENCE_CONDITION, REQUIREMENT
    }
}