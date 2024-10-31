package uk.gov.justice.digital.hmpps.api.model.appointment

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.ZonedDateTime
import java.util.*

data class CreateAppointment(
    val type: Type,
    val start: ZonedDateTime,
    val end: ZonedDateTime?,
    val interval: Int,
    val eventId: Long,
    val requirementId: Long? = null,
    val licenceConditionId: Long? = null,
    val numberOfAppointments: Int? = null,
    val until: ZonedDateTime? = null,
    //UUID should be provided by the f/e
    //to prevent accidental duplicates e.g. if a request is retried
    val uuid: UUID = UUID.randomUUID()
) {
    @JsonIgnore
    val urn = URN_PREFIX + uuid

    enum class Type(val code: String) {
        HomeVisitToCaseNS("CHVS"),
        InitialAppointmentInOfficeNS("COAI"),
        PlannedOfficeVisitNS("COAP"),
        InitialAppointmentHomeVisitNS("COHV")
    }

    companion object {
        const val URN_PREFIX = "urn:uk:gov:hmpps:manage-supervision-service:appointment:"
    }
}
