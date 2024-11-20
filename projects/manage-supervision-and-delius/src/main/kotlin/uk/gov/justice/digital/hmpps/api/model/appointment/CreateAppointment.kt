package uk.gov.justice.digital.hmpps.api.model.appointment

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.ZonedDateTime
import java.util.*

data class CreateAppointment(
    val user: User,
    val type: Type,
    val start: ZonedDateTime,
    val end: ZonedDateTime,
    val interval: Interval = Interval.DAY,
    val numberOfAppointments: Int = 1,
    val eventId: Long,
    val uuid: UUID,
    val requirementId: Long? = null,
    val licenceConditionId: Long? = null,
    val until: ZonedDateTime? = null
) {
    @JsonIgnore
    val urn = URN_PREFIX + uuid

    enum class Type(val code: String) {
        HomeVisitToCaseNS("CHVS"),
        InitialAppointmentInOfficeNS("COAI"),
        PlannedOfficeVisitNS("COAP"),
        InitialAppointmentHomeVisitNS("COHV")
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
    val team: String
)
