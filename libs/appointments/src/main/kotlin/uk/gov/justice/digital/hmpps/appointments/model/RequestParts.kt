package uk.gov.justice.digital.hmpps.appointments.model

import java.time.ZonedDateTime

interface AppointmentRequest {
    val externalReference: String
    val type: RequestCode
    val schedule: Schedule
    val relatedTo: ReferencedEntities
    val assigned: Assigned?
    val notes: String?
    val flagAs: FlagAs
}

data class Schedule(
    val start: ZonedDateTime,
    val end: ZonedDateTime,
) {
    fun isInTheFuture() = start.isAfter(ZonedDateTime.now())
}

data class Assigned(
    val location: RequestCode?,
    val team: RequestCode?,
    val staff: RequestCode?,
)

data class RequestCode(val code: String)

data class ReferencedEntities(
    val crn: String? = null,
    val personId: Long? = null,
    val eventId: Long? = null,
    val nonStatutoryInterventionId: Long? = null,
    val licenceConditionId: Long? = null,
    val requirementId: Long? = null,
    val pssRequirementId: Long? = null,
) {
    init {
        require(personId != null || crn != null) { "Either personId or crn must be provided" }
        require((requirementId == null && licenceConditionId == null && pssRequirementId == null) || eventId != null) {
            "eventId must be provided when requirementId, licenceConditionId or pssRequirementId is set"
        }
    }
}

data class FlagAs(
    val sensitive: Boolean,
    val visor: Boolean,
)