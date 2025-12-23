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
    val person: RelatedIdentifier<String>,
    val event: RelatedIdentifier<Long>?,
    val requirement: RelatedIdentifier<Long>?,
    val licenceCondition: RelatedIdentifier<Long>?,
)

data class RelatedIdentifier<T>(val id: T)

data class FlagAs(
    val sensitive: Boolean,
    val visor: Boolean,
)