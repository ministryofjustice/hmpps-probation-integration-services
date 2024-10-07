package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class Attendances(
    val attendances: List<Attendance> = emptyList()
)

data class Attendance(
    val attended: Boolean,
    val complied: Boolean,
    val attendanceDate: LocalDate,
    val contactId: Long,
    val outcome: String?,
    val contactType: ContactTypeDetail
)

data class ContactTypeDetail(
    val code: String?,
    val description: String?
)