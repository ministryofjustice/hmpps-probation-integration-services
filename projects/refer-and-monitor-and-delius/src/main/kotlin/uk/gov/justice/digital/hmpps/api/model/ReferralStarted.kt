package uk.gov.justice.digital.hmpps.api.model

import java.time.ZonedDateTime
import java.util.UUID

class ReferralStarted(
    val referralId: UUID,
    val startedAt: ZonedDateTime,
    val contractType: String,
    val sentenceId: Long,
    val notes: String,
) {
    val urn = "urn:hmpps:interventions-referral:$referralId"
}
