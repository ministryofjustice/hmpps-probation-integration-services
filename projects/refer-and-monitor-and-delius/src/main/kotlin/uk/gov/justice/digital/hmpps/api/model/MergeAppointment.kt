package uk.gov.justice.digital.hmpps.api.model

import com.fasterxml.jackson.annotation.JsonAlias
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.service.Outcome
import java.time.ZonedDateTime
import java.util.UUID

data class MergeAppointment(
    val id: UUID,
    val referralId: UUID,
    val referralReference: String,
    val start: ZonedDateTime,
    val durationInMinutes: Long?,
    @JsonAlias("end")
    val endTime: ZonedDateTime?,
    val notes: String?,
    val officeLocationCode: String?,
    val countsTowardsRar: Boolean,
    val outcome: Outcome?,
    val sentenceId: Long?,
    val previousId: UUID?,
    val deliusId: Long?
) {
    val referralUrn
        get() = "urn:hmpps:interventions-referral:$referralId"
    val urn
        get() = "urn:hmpps:interventions-appointment:$id"

    val previousUrn
        get() = previousId?.let { "urn:hmpps:interventions-appointment:$it" }

    val typeCode
        get() = if (countsTowardsRar) ContactType.Code.CRSAPT else ContactType.Code.CRSSAA

    // Temporary logic until interventions-service is sending duration in all envs
    val end = durationInMinutes?.let { start.plusMinutes(it) } ?: endTime ?: start
}
