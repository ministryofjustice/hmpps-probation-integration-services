package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class ReallocationCaseView(
    val name: Name,
    val dateOfBirth: LocalDate,
    val gender: String?,
    val pncNumber: String?,
    val mainAddress: CvAddress?,
    val nextAppointmentDate: LocalDate?,
    val activeEvents: List<ActiveEvent>
) {
    data class ActiveEvent(
        val number: String,
        val failureToComplyCount: Int,
        val failureToComplyStartDate: LocalDate,
        val sentence: CvSentence?,
        val offences: List<CvOffence>,
        val requirements: List<CvRequirement>
    )
}