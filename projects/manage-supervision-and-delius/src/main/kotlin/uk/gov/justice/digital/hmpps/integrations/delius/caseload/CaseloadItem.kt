package uk.gov.justice.digital.hmpps.integrations.delius.caseload

import java.time.LocalDateTime

interface CaseloadItem {
    val offenderId: Long
    val crn: String
    val teamCode: String
    val dateOfBirth: LocalDateTime?
    val firstName: String
    val secondName: String?
    val thirdName: String?
    val surname: String
    val latestSentenceTypeDescription: String?
    val totalSentences: Long
    val nextAppointmentId: Long?
    val nextAppointmentDateTime: LocalDateTime?
    val nextAppointmentTypeDescription: String?
    val prevAppointmentId: Long?
    val prevAppointmentDateTime: LocalDateTime?
    val prevAppointmentTypeDescription: String?
}

interface TeamCaseloadItem {
    val crn: String
    val firstName: String
    val secondName: String?
    val thirdName: String?
    val surname: String
    val staffForename: String
    val staffSurname: String
    val staffCode: String
}