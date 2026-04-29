package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

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
    val notes: String?,
    val startDate: LocalDate,
    val endDate: LocalDate?,
)