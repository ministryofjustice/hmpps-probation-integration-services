package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class MostRecentRegistration(
    val id: Long,
    val type: CodedDescription,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val notes: String?,
    val documentsLinked: Boolean,
    val deregistered: Boolean
)

data class InformationPageResponse(
    val registration: MostRecentRegistration?
)