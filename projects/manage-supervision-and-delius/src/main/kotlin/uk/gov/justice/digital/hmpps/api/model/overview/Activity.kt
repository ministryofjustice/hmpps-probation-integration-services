package uk.gov.justice.digital.hmpps.api.model.overview

data class ActivityCount(
    val acceptableAbsenceCount: Int,
    val unacceptableAbsenceCount: Int,
    val attendedButDidNotComplyCount: Int,
    val outcomeNotRecordedCount: Int,
    val waitingForEvidenceCount: Int,
    val rescheduledCount: Int,
    val absentCount: Int,
    val rescheduledByStaffCount: Int,
    val rescheduledByPersonOnProbationCount: Int,
    val lettersCount: Int,
    val nationalStandardAppointmentsCount: Int,
    val compliedAppointmentsCount: Int,
)