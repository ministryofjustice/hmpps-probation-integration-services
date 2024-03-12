package uk.gov.justice.digital.hmpps.api.model.personalDetails

import java.time.ZonedDateTime

data class Document(
    val id: String,
    val name: String,
    val lastUpdated: ZonedDateTime?
)
