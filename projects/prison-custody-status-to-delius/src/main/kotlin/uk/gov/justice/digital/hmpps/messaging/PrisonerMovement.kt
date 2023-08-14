package uk.gov.justice.digital.hmpps.messaging

import java.time.ZonedDateTime

sealed interface PrisonerMovement {
    val nomsId: String
    val prisonId: String?
    val type: Type
    val reason: String
    val occurredAt: ZonedDateTime

    data class Received(
        override val nomsId: String,
        override val prisonId: String,
        override val type: Type,
        override val reason: String,
        override val occurredAt: ZonedDateTime
    ) : PrisonerMovement

    data class Released(
        override val nomsId: String,
        override val prisonId: String?,
        override val type: Type,
        override val reason: String,
        override val occurredAt: ZonedDateTime
    ) : PrisonerMovement

    enum class Type {
        ADMISSION,
        RELEASED,
        RELEASED_TO_HOSPITAL,
        RETURN_FROM_COURT,
        SENT_TO_COURT,
        TEMPORARY_ABSENCE_RELEASE,
        TEMPORARY_ABSENCE_RETURN,
        TRANSFERRED
    }

    fun isHospitalRelease() =
        this is Released && (
            type == Type.RELEASED_TO_HOSPITAL ||
                reason in listOf(
                MovementReasonCodes.DETAINED_MENTAL_HEALTH,
                MovementReasonCodes.RELEASE_MENTAL_HEALTH,
                MovementReasonCodes.FINAL_DISCHARGE_PSYCHIATRIC
            )
            )
}
