package uk.gov.justice.digital.hmpps.model

import jakarta.validation.constraints.NotEmpty
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
    val sensitive: Boolean
) {
    init {
        require((licenceConditionId == null && requirementId != null) || (requirementId == null && licenceConditionId != null)) {
            "Either licence condition or requirement id must be specified."
        }
    }
}

data class RequestCode(val code: String)