package uk.gov.justice.digital.hmpps.exception

import uk.gov.justice.digital.hmpps.integrations.delius.projections.ContactNotFoundReason
import uk.gov.justice.digital.hmpps.service.Outcome
import java.util.UUID

class AppointmentNotFoundException(
    val appointmentId: UUID,
    val deliusId: Long?,
    val referralReference: String,
    val outcome: Outcome,
    val reason: AppointmentNotFoundReason
) : UnprocessableException(
    "Appointment Not Found",
    mapOf(
        "appointmentId" to appointmentId.toString(),
        "deliusId" to deliusId.toString(),
        "referralReference" to referralReference,
        "outcomeAttended" to outcome.attended.toString(),
        "outcomeNotify" to outcome.notify.toString(),
        "reason" to reason.reason,
        "reasonDetail" to reason.additionalInformation
    )
)

fun ContactNotFoundReason?.asReason(): AppointmentNotFoundReason =
    AppointmentNotFoundReason(
        when {
            this == null -> "Unknown"
            softDeleted == null && nsiActive == 0 -> "NSI terminated, likely a future appointment deleted"
            softDeleted == null -> "Contact hard deleted or not yet present"
            softDeleted == 0 -> "Created with outcome using merge appointment at the same time"
            softDeleted == 1 -> "Contact soft deleted"
            nsiActive == 0 -> "NSI terminated"
            nsiActive == null -> "NSI cannot be determined"
            else -> "Unknown"
        },
        "NSI last updated by ${this?.nsiLastUpdatedBy}"
    )

data class AppointmentNotFoundReason(
    val reason: String,
    val additionalInformation: String
)
