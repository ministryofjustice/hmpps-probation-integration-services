package uk.gov.justice.digital.hmpps.model

import java.time.ZonedDateTime

data class PersonalCircumstances(
    val type: CodeDescription,
    val subType: CodeDescription?,
    val startDate: ZonedDateTime,
    val endDate: ZonedDateTime?,
    val verified: Boolean?,
    val notes: String?
)
