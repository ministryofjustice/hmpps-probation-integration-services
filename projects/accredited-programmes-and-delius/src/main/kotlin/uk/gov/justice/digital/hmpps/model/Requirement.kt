package uk.gov.justice.digital.hmpps.model

import java.time.ZonedDateTime

data class Requirement(
    val id: Long,
    val mainCategory: CodedValue?,
    val subCategory: CodedValue?,
    val manager: Manager,
    val probationDeliveryUnits: List<PduOfficeLocations>,
    val eventNumber: String,
    val createdAt: ZonedDateTime,
)

data class Requirements(
    val content: List<Requirement>,
)