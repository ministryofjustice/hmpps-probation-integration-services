package uk.gov.justice.digital.hmpps.model

import jakarta.validation.constraints.NotEmpty
import uk.gov.justice.digital.hmpps.entity.contact.ContactType
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

data class CreateAppointmentsRequest(@NotEmpty val appointments: List<CreateAppointmentRequest>)
data class CreateAppointmentRequest(
    val reference: UUID,
    val requirementId: Long?,
    val licenceConditionId: Long?,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val outcome: RequestCode?,
    val location: RequestCode?,
    val staff: RequestCode,
    val team: RequestCode,
    val notes: String?,
    val sensitive: Boolean,
    val type: Type = Type.PROGRAMME_ATTENDANCE
) {
    init {
        require((licenceConditionId == null && requirementId != null) || (requirementId == null && licenceConditionId != null)) {
            "Either licence condition or requirement id must be specified."
        }
    }

    enum class Type(val code: String) {
        PROGRAMME_ATTENDANCE(ContactType.APPOINTMENT),
        THREE_WAY_MEETING(ContactType.THREE_WAY_MEETING),
    }
}

data class RequestCode(val code: String)