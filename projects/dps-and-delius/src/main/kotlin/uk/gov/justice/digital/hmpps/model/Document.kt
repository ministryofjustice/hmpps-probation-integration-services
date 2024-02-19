package uk.gov.justice.digital.hmpps.model

import java.time.ZonedDateTime

data class Document(
    val id: String,
    val name: String,
    val description: String?,
    val type: String,
    val author: String?,
    val createdAt: ZonedDateTime?
)
