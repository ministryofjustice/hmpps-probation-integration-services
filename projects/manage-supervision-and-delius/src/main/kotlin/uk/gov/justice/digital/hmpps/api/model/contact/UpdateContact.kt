package uk.gov.justice.digital.hmpps.api.model.contact

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

data class UpdateContact(
    val dateTime: ZonedDateTime,
    val notes: String?,
    val sensitiveFlag: Boolean?
)

data class UpdateContactOutcome(
    val date: LocalDate,
    val time: LocalTime,
    val outcomeCode: String?,
    val enforcementActionCode: String?,
    val notes: String,
    val alert: Boolean,
    val sensitive: Boolean
)