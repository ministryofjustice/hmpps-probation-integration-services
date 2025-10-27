package uk.gov.justice.digital.hmpps.api.model.appointment

import com.fasterxml.jackson.annotation.JsonIgnore
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.Appointment.Companion.URN_PREFIX
import java.time.ZonedDateTime
import java.util.*

data class CreateAppointment(
    val user: User,
    val type: String,
    val start: ZonedDateTime,
    val end: ZonedDateTime,
    val eventId: Long? = null,
    val uuid: UUID,
    val requirementId: Long? = null,
    val licenceConditionId: Long? = null,
    val nsiId: Long? = null,
    val until: ZonedDateTime? = null,
    val notes: String? = null,
    val sensitive: Boolean? = null,
    val visorReport: Boolean? = null,
) {
    @JsonIgnore
    val urn = URN_PREFIX + uuid

    enum class Type(val code: String) {
        PlannedOfficeVisitNS("COAP"),
        PlannedTelephoneContactNS("COPT"),
        PlannedVideoContactNS("COVC"),
        PannedContactOtherThanOffice("COOO"),
        InitialAppointmentInOfficeNS("COAI"),
        HomeVisitToCaseNS("CHVS"),
        ThreeWayMeetingNS("C084"),
        PlannedDoorstepContactNS("CODC"),
        InterviewForReportOther("COSR")
    }

    enum class Interval(val value: Int) {
        DAY(1),
        WEEK(7),
        FORTNIGHT(14),
        FOUR_WEEKS(28)
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
