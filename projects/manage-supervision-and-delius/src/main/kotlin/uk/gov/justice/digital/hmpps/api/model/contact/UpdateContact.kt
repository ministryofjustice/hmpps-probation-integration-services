package uk.gov.justice.digital.hmpps.api.model.contact

import java.time.ZonedDateTime

data class UpdateContact(
    val dateTime: ZonedDateTime,
    val notes: String?,
    val sensitiveFlag: Boolean?
)