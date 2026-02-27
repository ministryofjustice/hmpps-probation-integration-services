package uk.gov.justice.digital.hmpps.model.request

class CreateAppointment {
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
