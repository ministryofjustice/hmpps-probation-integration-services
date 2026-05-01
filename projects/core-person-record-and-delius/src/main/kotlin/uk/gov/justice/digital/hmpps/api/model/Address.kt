package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate
import java.time.ZonedDateTime

data class Address(
    val id: Long,
    val fullAddress: String,
    val buildingName: String?,
    val addressNumber: String?,
    val streetName: String?,
    val district: String?,
    val townCity: String?,
    val county: String?,
    val postcode: String,
    val uprn: Long?,
    val telephoneNumber: String?,
    val noFixedAbode: Boolean,
    val status: CodeDescription,
    val type: CodeDescription?,
    val typeVerified: Boolean?,
    val notes: String?,
    val startDateTime: ZonedDateTime?,
    val endDateTime: ZonedDateTime?,
    val startDate: LocalDate? = startDateTime?.toLocalDate(),
    val endDate: LocalDate? = endDateTime?.toLocalDate(),
)