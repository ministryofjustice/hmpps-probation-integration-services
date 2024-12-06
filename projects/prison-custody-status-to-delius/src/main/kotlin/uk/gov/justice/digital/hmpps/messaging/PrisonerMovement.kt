package uk.gov.justice.digital.hmpps.messaging

import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.Custody
import java.time.ZonedDateTime

sealed interface PrisonerMovement {
    val nomsId: String
    val fromPrisonId: String?
    val toPrisonId: String?
    val type: Type
    val reason: String
    val occurredAt: ZonedDateTime
    var reasonOverride: String?

    data class Received(
        override val nomsId: String,
        override val fromPrisonId: String?,
        override val toPrisonId: String,
        override val type: Type,
        override val reason: String,
        override val occurredAt: ZonedDateTime,
        override var reasonOverride: String? = null
    ) : PrisonerMovement

    data class Released(
        override val nomsId: String,
        override val fromPrisonId: String,
        override val toPrisonId: String?,
        override val type: Type,
        override val reason: String,
        override val occurredAt: ZonedDateTime,
        override var reasonOverride: String? = null
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

    fun isHospitalRelease() = this is Released && (
        type == Type.RELEASED_TO_HOSPITAL || reason in listOf(
            MovementReasonCodes.DETAINED_MENTAL_HEALTH,
            MovementReasonCodes.RELEASE_MENTAL_HEALTH,
            MovementReasonCodes.FINAL_DISCHARGE_PSYCHIATRIC
        )
        )

    fun isIrcRelease() = this is Released && reason in listOf(
        MovementReasonCodes.DISCHARGED_OR_DEPORTED,
        MovementReasonCodes.DEPORTED_NO_SENTENCE,
        MovementReasonCodes.DEPORTED_LICENCE,
        MovementReasonCodes.DEPORTED_IRC,
        MovementReasonCodes.EARLY_REMOVAL_SCHEME,
        MovementReasonCodes.END_CUSTODY_TO_IMMIGRATION_RELEASE_CENTRE
    )

    fun isAbsconded() = this is Released && reason in listOf(
        MovementReasonCodes.ABSCONDED,
        MovementReasonCodes.ABSCONDED_ECL
    )
}

fun PrisonerMovement.releaseDateValid(custody: Custody): Boolean {
    val release = custody.mostRecentRelease()
    val recallDate = release?.recall?.date
    return occurredAt >= custody.disposal.date && (release == null || (recallDate != null && occurredAt >= recallDate))
}

fun PrisonerMovement.receivedDateValid(custody: Custody): Boolean {
    val releaseDate = custody.mostRecentRelease()?.date
    return occurredAt <= ZonedDateTime.now() && (releaseDate == null || occurredAt >= releaseDate)
}

fun PrisonerMovement.statusDateValid(custody: Custody): Boolean =
    occurredAt <= ZonedDateTime.now() && occurredAt.toLocalDate() >= custody.statusChangeDate

fun PrisonerMovement.locationDateValid(custody: Custody): Boolean =
    occurredAt <= ZonedDateTime.now() && (custody.locationChangeDate == null || occurredAt.toLocalDate() >= custody.locationChangeDate)
