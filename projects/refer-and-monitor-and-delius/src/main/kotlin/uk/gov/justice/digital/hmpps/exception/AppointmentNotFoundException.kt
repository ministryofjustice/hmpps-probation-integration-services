package uk.gov.justice.digital.hmpps.exception

import jakarta.persistence.Tuple
import uk.gov.justice.digital.hmpps.service.Outcome
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
                when {
                    it["contact_soft_deleted"].asInt() == 1 -> "Contact soft-deleted"
                    it["contact_soft_deleted"].asInt() == 0 -> "Contact not soft-deleted"
                    it["nsi_soft_deleted"].asInt() == 1 -> "NSI soft-deleted"
                    it["nsi_active"].asInt() == 0 -> "NSI terminated"
                    else -> "Unknown"
                },
                "NSI last updated by ${it["nsi_last_updated_by"]}"
            )
        } ?: unknown

        private val unknown = AppointmentNotFoundReason("Unknown", "Unknown")

        private fun Any?.asInt() = (this as Number?)?.toInt()
    }
}
