package uk.gov.justice.digital.hmpps.messaging

import java.time.ZonedDateTime

sealed interface PrisonerMovement {
    val nomsId: String
    val prisonId: String?
    val reason: String
    val movementReason: String
    val occurredAt: ZonedDateTime

    data class Received(
        override val nomsId: String,
        override val prisonId: String,
        override val reason: String,
        override val movementReason: String,
        override val occurredAt: ZonedDateTime
    ) : PrisonerMovement

    data class Released(
        override val nomsId: String,
        override val prisonId: String?,
        override val reason: String,
        override val movementReason: String,
        override val occurredAt: ZonedDateTime
    ) : PrisonerMovement
}
