package uk.gov.justice.digital.hmpps.api.model.appointment

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.Positive
import java.time.ZonedDateTime
import java.util.*

data class CreateAppointment(
    val user: User,
    val type: Type,
    val start: ZonedDateTime,
    val end: ZonedDateTime,
    val interval: Interval = Interval.DAY,
    @field:Positive(message = "number of appointments must be greater than or equal to 1")
    val numberOfAppointments: Int = 1,
    val eventId: Long? = null,
    val uuid: UUID,
    val createOverlappingAppointment: Boolean = false,
    val requirementId: Long? = null,
    val licenceConditionId: Long? = null,
    val nsiId: Long? = null,
    val until: ZonedDateTime? = null,
) {
    @JsonIgnore
    val urn = URN_PREFIX + uuid

    enum class Type(val code: String) {
        PlannedOfficeVisitNS("COAP"),
        PlannedTelephoneContactNS("COPT"),
        InitialAppointmentInOfficeNS("COAI"),
        PlannedVideoContactNS("COVC"),
        PannedContactOtherThanOffice("COOO"),
        PlannedDoorstepContactNS("CODC"),
        InitialAppointmentHomeVisitNS("COHV"),
        HomeVisitToCaseNS("CHVS"),
        ThreeWayMeetingNS("C084")
    }

    enum class Interval(val value: Int) {
        DAY(1),
        WEEK(7),
        FORTNIGHT(14),
        FOUR_WEEKS(28)
    }

    companion object {
        const val URN_PREFIX = "urn:uk:gov:hmpps:manage-supervision-service:appointment:"
    }
}

data class User(
    val username: String,
    val teamCode: String,
    val locationCode: String? = null,
)

data class OverlappingAppointment(
    val start: String,
    val end: String,
)
