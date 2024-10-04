package uk.gov.justice.digital.hmpps.api.model

import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactOutcome.Code
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.service.Outcome
import java.time.ZonedDateTime
import java.util.UUID

data class MergeAppointment(
    val id: UUID,
    val referralId: UUID,
    val referralReference: String,
    val start: ZonedDateTime,
    val durationInMinutes: Long,
    val notes: String?,
    val officeLocationCode: String?,
    val countsTowardsRar: Boolean,
    val outcome: Outcome?,
    val sentenceId: Long?,
    val previousId: UUID?,
    val deliusId: Long?,
    val rescheduleRequestedBy: String?,
) {
    val referralUrn
        get() = "urn:hmpps:interventions-referral:$referralId"
    val urn
        get() = "urn:hmpps:interventions-appointment:$id"

    val previousUrn
        get() = previousId?.let { "urn:hmpps:interventions-appointment:$it" }

    val typeCode
        get() = if (countsTowardsRar) ContactType.Code.CRSAPT else ContactType.Code.CRSSAA

    val end: ZonedDateTime = start.plusMinutes(durationInMinutes)

    val rescheduleOutcome
        get() = when (rescheduleRequestedBy) {
            null, "Service Provider" -> Code.RESCHEDULED_SERVICE_REQUEST.value
            else -> Code.RESCHEDULED_POP_REQUEST.value
        }
}
