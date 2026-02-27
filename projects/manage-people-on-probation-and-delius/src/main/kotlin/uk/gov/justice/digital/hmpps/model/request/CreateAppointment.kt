package uk.gov.justice.digital.hmpps.model.request

import java.time.ZonedDateTime
import java.util.*

data class CreateAppointment(
    val uuid: UUID,
    val user: User,
    val type: String,
    val start: ZonedDateTime,
    val end: ZonedDateTime,
    val eventId: Long? = null,
    val requirementId: Long? = null,
    val licenceConditionId: Long? = null,
    val nsiId: Long? = null,
    val until: ZonedDateTime? = null,
    val notes: String? = null,
    val sensitive: Boolean? = null,
    val visorReport: Boolean? = null,
    val outcomeRecorded: Boolean = false,
) {
    data class User(
        val username: String,
        val teamCode: String,
        val locationCode: String? = null,
    )

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
}
