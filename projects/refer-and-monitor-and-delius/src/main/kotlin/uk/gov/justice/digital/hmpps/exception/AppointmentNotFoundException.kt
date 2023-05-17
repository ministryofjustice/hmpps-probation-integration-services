package uk.gov.justice.digital.hmpps.exception

import jakarta.persistence.Tuple
import uk.gov.justice.digital.hmpps.service.Outcome
import java.math.BigDecimal
import java.util.UUID

class AppointmentNotFoundException(
    val appointmentId: UUID,
    val deliusId: Long?,
    val referralReference: String,
    val outcome: Outcome,
    val reason: AppointmentNotFoundReason
) : UnprocessableException(
    "AppointmentNotFound",
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

data class AppointmentNotFoundReason(
    val reason: String,
    val additionalInformation: String
) {
    companion object {
        fun from(result: Tuple?) = result?.let {
            AppointmentNotFoundReason(
                if (it["soft_deleted"] == BigDecimal.ONE) {
                    "NSI Deleted"
                } else if (it["active_flag"] == BigDecimal.ZERO) {
                    "NSI Terminated"
                } else {
                    "Unknown"
                },
                "NSI last updated by ${it["distinguished_name"]}"
            )
        } ?: unknown

        private val unknown = AppointmentNotFoundReason("Unknown", "Unknown")
    }
}
